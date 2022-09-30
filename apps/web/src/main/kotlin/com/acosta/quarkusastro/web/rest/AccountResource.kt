package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.service.*
import com.acosta.quarkusastro.service.dto.PasswordChangeDTO
import com.acosta.quarkusastro.service.dto.UserDTO
import com.acosta.quarkusastro.web.rest.errors.AccountResourceException
import com.acosta.quarkusastro.web.rest.errors.EmailNotFoundException
import com.acosta.quarkusastro.web.rest.errors.InvalidPasswordWebException
import com.acosta.quarkusastro.web.rest.vm.KeyAndPasswordVM
import com.acosta.quarkusastro.web.rest.vm.ManagedUserVM
import io.quarkus.security.Authenticated
import org.slf4j.LoggerFactory
import java.security.Principal
import java.util.*
import java.util.concurrent.CompletionStage
import javax.annotation.security.PermitAll
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


/**
 * REST controller for managing the current user's account.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
class AccountResource @Inject constructor(
    private val mailService: MailService, private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(AccountResource::class.java)

    /**
     * `GET /account` : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException `500 (Internal Server Error)` if the user couldn't be returned.
     */
    @GET
    @Path("/account")
    @Authenticated
    fun getAccount(@Context ctx: SecurityContext): UserDTO {
        val user = userService.getUserWithAuthoritiesByLogin(ctx.userPrincipal.name)
            ?: throw AccountResourceException("User could not be found")
        return UserDTO(user)
    }

    /**
     * `POST /account` : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException `400 (Bad Request)` if the email is already used.
     * @throws RuntimeException          `500 (Internal Server Error)` if the user login wasn't found.
     */
    @POST
    @Path("/account")
    fun saveAccount(userDTO: @Valid UserDTO, @Context ctx: SecurityContext): Response {
        val userLogin = Optional.ofNullable(ctx.userPrincipal.name).orElseThrow {
                AccountResourceException(
                    "Current user login not found"
                )
            }
        val existingUser = userService.findOneByEmailIgnoreCase(userDTO.email)
        if (existingUser != null && existingUser.login != userLogin) {
            throw EmailAlreadyUsedException()
        }

        userService.findOneByLogin(userLogin)
            ?: throw AccountResourceException("User could not be found")

        userService.updateUser(
            userLogin,
            userDTO.firstName,
            userDTO.lastName,
            userDTO.email,
            userDTO.langKey,
            userDTO.imageUrl
        )
        return Response.ok().build()
    }

    /**
     * `POST /register` : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordWebException  `400 (Bad Request)` if the password is incorrect.
     * @throws EmailAlreadyUsedException `400 (Bad Request)` if the email is already used.
     * @throws LoginAlreadyUsedException `400 (Bad Request)` if the login is already used.
     */
    @POST
    @Path("/register")
    @PermitAll
    fun registerAccount(managedUserVM: @Valid ManagedUserVM): CompletionStage<Response> {
        if (!checkPasswordLength(managedUserVM.password)) {
            throw InvalidPasswordWebException()
        }
        return try {
            val user = userService.registerUser(managedUserVM, managedUserVM.password)
            mailService.sendActivationEmail(user).thenApply {
                Response.created(
                    null
                ).build()
            }
        } catch (e: UsernameAlreadyUsedException) {
            throw LoginAlreadyUsedException()
        } catch (e: EmailAlreadyUsedException) {
            throw EmailAlreadyUsedException()
        }
    }

    /**
     * `GET /activate` : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException `500 (Internal Server Error)` if the user couldn't be activated.
     */
    @GET
    @Path("/activate")
    @PermitAll
    fun activateAccount(@QueryParam(value = "key") key: String) {
        val user = userService.activateRegistration(key)
        if (user != null) {
            throw AccountResourceException("No user was found for this activation key")
        }
    }

    /**
     * `GET /authenticate` : check if the user is authenticated, and return its login.
     *
     * @param ctx the request security context.
     * @return the login if the user is authenticated.
     */
    @GET
    @Path("/authenticate")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    fun isAuthenticated(@Context ctx: SecurityContext): String {
        log.debug("REST request to check if the current user is authenticated")
        return Optional.ofNullable(ctx.userPrincipal).map { obj: Principal -> obj.name }.orElse("")
    }

    /**
     * `POST /account/change-password` : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordWebException `400 (Bad Request)` if the new password is incorrect.
     */
    @POST
    @Path("/account/change-password")
    fun changePassword(
        passwordChangeDto: PasswordChangeDTO, @Context ctx: SecurityContext
    ): Response {
        val userLogin = Optional.ofNullable(ctx.userPrincipal.name).orElseThrow {
                AccountResourceException(
                    "Current user login not found"
                )
            }
        if (!checkPasswordLength(passwordChangeDto.newPassword)) {
            throw InvalidPasswordWebException()
        }
        try {
            userService.changePassword(
                userLogin, passwordChangeDto.currentPassword, passwordChangeDto.newPassword
            )
        } catch (e: InvalidPasswordException) {
            throw InvalidPasswordWebException()
        }
        return Response.ok().build()
    }

    /**
     * `POST /account/reset-password/init` : Email reset the password of the user.
     *
     * @param mail the mail of the user.
     * @throws EmailNotFoundException `400 (Bad Request)` if the email address is not registered.
     */
    @POST
    @Path("/account/reset-password/init")
    @Consumes(MediaType.TEXT_PLAIN)
    fun requestPasswordReset(mail: String): Response {
        mailService.sendPasswordResetMail(
            userService.requestPasswordReset(mail)
        )
        return Response.ok().build()
    }

    /**
     * `POST /account/reset-password/finish` : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordWebException `400 (Bad Request)` if the password is incorrect.
     * @throws RuntimeException         `500 (Internal Server Error)` if the password could not be reset.
     */
    @POST
    @Path("/account/reset-password/finish")
    fun finishPasswordReset(keyAndPassword: KeyAndPasswordVM): Response {
        if (!checkPasswordLength(keyAndPassword.newPassword)) {
            throw InvalidPasswordWebException()
        }
        userService.completePasswordReset(keyAndPassword.newPassword, keyAndPassword.key)
        return Response.ok().build()
    }

    companion object {
        private fun checkPasswordLength(password: String): Boolean {
            return password.isNotEmpty() && (password.length >= ManagedUserVM.PASSWORD_MIN_LENGTH && password.length <= ManagedUserVM.PASSWORD_MAX_LENGTH)
        }
    }
}
