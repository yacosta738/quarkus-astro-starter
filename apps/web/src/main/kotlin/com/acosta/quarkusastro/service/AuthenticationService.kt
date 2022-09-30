package com.acosta.quarkusastro.service

import com.acosta.quarkusastro.domain.User
import com.acosta.quarkusastro.repository.AuthorityRepository
import com.acosta.quarkusastro.repository.UserRepository
import com.acosta.quarkusastro.security.BCryptPasswordHasher
import com.acosta.quarkusastro.security.UserNotActivatedException
import com.acosta.quarkusastro.security.UsernameNotFoundException
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.credential.PasswordCredential
import io.quarkus.security.runtime.QuarkusPrincipal
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import org.slf4j.LoggerFactory
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
class AuthenticationService @Inject constructor(private val passwordHasher: BCryptPasswordHasher) {
    private val log = LoggerFactory.getLogger(AuthenticationService::class.java)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authorityRepository: AuthorityRepository

    fun authenticate(login: String, password: String): QuarkusSecurityIdentity {
        val user: User = loadByUsername(login)
        if (!user.activated) {
            throw UserNotActivatedException("User $login was not activated")
        }
        if (user.password.let { passwordHasher.checkPassword(password, it) }) {
            return createQuarkusSecurityIdentity(user)
        }
        log.debug("Authentication failed: password does not match stored value")
        throw AuthenticationFailedException("Authentication failed: password does not match stored value")
    }

    private fun loadByUsername(login: String): User {
        log.debug("Authenticating {}", login)
        if (login.matches(Regex(emailValidator))) {
            return userRepository
                .findOneWithAuthoritiesByEmailIgnoreCase(login)
                ?: throw UsernameNotFoundException("User with email $login was not found in the database")
        }
        val lowercaseLogin = login.lowercase()
        return userRepository
            .findOneWithAuthoritiesByLogin(lowercaseLogin)
            ?: throw UsernameNotFoundException("User $lowercaseLogin was not found in the database")
    }

    private fun createQuarkusSecurityIdentity(user: User): QuarkusSecurityIdentity {
        val builder = QuarkusSecurityIdentity.builder()
        builder.setPrincipal(QuarkusPrincipal(user.login))
        builder.addCredential(PasswordCredential(user.password?.toCharArray() ?: charArrayOf()))
        builder.addRoles(
            user.authorities.stream().map { authority -> authority.name }.collect(
                Collectors.toSet()
            )
        )
        return builder.build()
    }

    companion object {
        const val emailValidator: String =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    }
}
