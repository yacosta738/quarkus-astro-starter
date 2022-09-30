package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.domain.User
import com.acosta.quarkusastro.security.AuthoritiesConstants
import com.acosta.quarkusastro.service.EmailAlreadyUsedException
import com.acosta.quarkusastro.service.LoginAlreadyUsedException
import com.acosta.quarkusastro.service.MailService
import com.acosta.quarkusastro.service.UserService
import com.acosta.quarkusastro.service.dto.UserDTO
import com.acosta.quarkusastro.web.rest.errors.BadRequestAlertException
import com.acosta.quarkusastro.web.util.HeaderUtil
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder.fromPath


/**
 * REST controller for managing users.
 *
 *
 * This class accesses the [User] entity, and needs to fetch its collection of authorities.
 *
 *
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 *
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 *  * We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.
 *  *  Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).
 *  *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 *
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
class UserResource @Inject constructor(
    @param:ConfigProperty(name = "application.name") val applicationName: String,
    private val mailService: MailService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(UserResource::class.java)

    /**
     * `POST  /users`  : Creates a new user.
     *
     *
     * Creates a new user if the login and email are not already used, and sends a
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the [Response] with status `201 (Created)` and with body the new user, or with status `400 (Bad Request)` if the login or email is already in use.
     * @throws BadRequestAlertException `400 (Bad Request)` if the login or email is already in use.
     */
    @POST
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    fun createUser(userDTO: @Valid UserDTO): Response {
        log.debug("REST request to save User : {}", userDTO)
        if (userDTO.id != null) {
            throw BadRequestAlertException(
                "A new user cannot already have an ID",
                "userManagement",
                "idexists"
            )
            // Lowercase the user login before comparing with database
        }
        if (userService.findOneByLogin(userDTO.login.lowercase(Locale.getDefault())) != null) {
            throw LoginAlreadyUsedException()
        }
        if (userService.findOneByEmailIgnoreCase(userDTO.email) != null) {
            throw EmailAlreadyUsedException()
        }

        val newUser = userService.createUser(userDTO)
        mailService.sendCreationEmail(newUser)
        val response =
            Response.created(fromPath("/api/users").path(newUser.login).build()).entity(newUser)
        HeaderUtil.createAlert(applicationName, "userManagement.created", newUser.login)
            .forEach { (s: String, o: Any) ->
                response.header(
                    s,
                    o
                )
            }
        return response.build()

    }

    /**
     * `PUT /users` : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the [Response] with status `200 (OK)` and with body the updated user.
     * @throws EmailAlreadyUsedException `400 (Bad Request)` if the email is already in use.
     * @throws LoginAlreadyUsedException `400 (Bad Request)` if the login is already in use.
     */
    @PUT
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    fun updateUser(userDTO: @Valid UserDTO): Response {
        log.debug("REST request to update User : {}", userDTO)
         userService.findOneByEmailIgnoreCase(userDTO.email)
            ?: throw EmailAlreadyUsedException()
        val existingUser = userService.findOneByLogin(userDTO.login.lowercase(Locale.getDefault()))
        if (existingUser != null && existingUser.id != userDTO.id) {
            throw LoginAlreadyUsedException()
        }
        val updatedUser = userService.updateUser(userDTO)

        val response = Response.ok(updatedUser)
        HeaderUtil.createAlert(applicationName, "userManagement.updated", userDTO.login)
            .forEach { (s: String, o: Any) ->
                response.header(
                    s,
                    o
                )
            }
        return response.build()
    }

    /**
     * `DELETE /users/:login` : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the [Response] with status `204 (NO_CONTENT)`.
     */
    @DELETE
    @Path("/{login}")
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    fun deleteUser(@PathParam("login") login: String): Response {
        log.debug("REST request to delete User: {}", login)
        userService.deleteUser(login)
        val response = Response.noContent()
        HeaderUtil.createAlert(applicationName, "userManagement.deleted", login)
            .forEach { (s: String, o: Any) ->
                response.header(
                    s,
                    o
                )
            }
        return response.build()
    }

    /**
     * `GET /users` : get all users.
     *
     * @param pagination the pagination information.
     * @return the [Response] with status `200 (OK)` and with body all users.
     */
    @GET
    fun getAllUsers(@QueryParam("sort") pagination: String): Response {
        val page: List<UserDTO> = userService.getAllManagedUsers()
        //        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        val response = Response.ok(page)
        return response.build()
    }

    /**
     * Gets a list of all roles.
     *
     * @return a string list of all roles.
     */
    @get:RolesAllowed("ROLE_ADMIN")
    @get:Path("/authorities")
    @get:GET
    val authorities: List<String>
        get() = userService.getAuthorities()

    /**
     * `GET /users/:login` : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the [Response] with status `200 (OK)` and with body the "login" user, or with status `404 (Not Found)`.
     */
    @GET
    @Path("/{login}")
    fun getUser(@PathParam("login") login: String): Response {
        log.debug("REST request to get User : {}", login)
        val userWithAuthorities: User? = userService.getUserWithAuthoritiesByLogin(login)
        return if (userWithAuthorities != null) {
            val response = Response.ok(UserDTO(userWithAuthorities))
            response.build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    companion object {
        private const val ENTITY_NAME = "users"
    }
}
