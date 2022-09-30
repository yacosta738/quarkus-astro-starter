package com.acosta.quarkusastro.repository

import com.acosta.quarkusastro.domain.User
import io.quarkus.cache.CacheResult
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.panache.common.Page
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository : PanacheRepository<User> {
    companion object {
        private const val serialVersionUID = 1L
        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
        const val USERS_BY_LOGIN_CACHE = "usersByLogin"
    }

    fun findOneByActivationKey(activationKey: String) =
        find("activationKey", activationKey).firstResult()

    fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(dateTime: Instant) =
        list("activated = false and activationKey is not null and createdDate <= ?1", dateTime)

    fun findOneByResetKey(resetKey: String) =
        find("resetKey", resetKey).firstResult()

    fun findOneByEmailIgnoreCase(email: String) =
        find("LOWER(email) = LOWER(?1)", email).firstResult()

    fun findOneByLogin(login: String) =
        find("login", login).firstResult()

    fun findOneWithAuthoritiesById(id: Long) =
        find("FROM User u LEFT JOIN FETCH u.authorities WHERE u.id = ?1", id).firstResult()

    @CacheResult(cacheName = USERS_BY_LOGIN_CACHE)
    fun findOneWithAuthoritiesByLogin(login: String) =
        find("FROM User u LEFT JOIN FETCH u.authorities WHERE u.login = ?1", login).firstResult()

    @CacheResult(cacheName = USERS_BY_EMAIL_CACHE)
    fun findOneWithAuthoritiesByEmailIgnoreCase(email: String) =
        find(
            "FROM User u LEFT JOIN FETCH u.authorities WHERE LOWER(u.login) = LOWER(?1)",
            email
        ).firstResult()

    fun findAllByLoginNot(page: Page, login: String) =
        find("login != ?1", login).page(page).list()
}