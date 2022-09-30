package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.security.jwt.TokenProvider
import com.acosta.quarkusastro.service.AuthenticationService
import com.acosta.quarkusastro.web.rest.vm.LoginVM
import io.quarkus.runtime.annotations.RegisterForReflection
import io.quarkus.security.UnauthorizedException
import org.slf4j.LoggerFactory
import javax.annotation.security.PermitAll
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.json.bind.annotation.JsonbProperty
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


/**
 * Controller to authenticate users.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
class UserJWTController @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val tokenProvider: TokenProvider
) {
    private val log = LoggerFactory.getLogger(UserJWTController::class.java)
    @POST
    @Path("/authenticate")
    @PermitAll
    fun authorize(loginVM: @Valid LoginVM): Response {
        return try {
            val identity = authenticationService.authenticate(loginVM.username, loginVM.password)
            val rememberMe = loginVM.rememberMe ?: false
            val jwt = tokenProvider.createToken(identity, rememberMe)
            Response.ok().entity(JWTToken(jwt)).header(
                "Authorization",
                "Bearer $jwt"
            ).build()
        } catch (e: SecurityException) {
            throw UnauthorizedException()
        }
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    @RegisterForReflection
    class JWTToken internal constructor(@field:JsonbProperty("id_token") val idToken: String)
}
