package com.acosta.quarkusastro.service

import com.acosta.quarkusastro.config.Constants
import com.acosta.quarkusastro.domain.Authority
import com.acosta.quarkusastro.domain.User
import com.acosta.quarkusastro.repository.AuthorityRepository
import com.acosta.quarkusastro.repository.UserRepository
import com.acosta.quarkusastro.security.AuthoritiesConstants
import com.acosta.quarkusastro.security.BCryptPasswordHasher
import com.acosta.quarkusastro.security.RandomUtil.generateActivationKey
import com.acosta.quarkusastro.security.RandomUtil.generatePassword
import com.acosta.quarkusastro.security.RandomUtil.generateResetKey
import com.acosta.quarkusastro.security.UserNotActivatedException
import com.acosta.quarkusastro.service.dto.UserDTO
import com.acosta.quarkusastro.web.rest.errors.AccountResourceException
import com.acosta.quarkusastro.web.rest.errors.EmailNotFoundException
import io.quarkus.cache.CacheInvalidate
import io.quarkus.panache.common.Page
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional


@ApplicationScoped
@Transactional
class UserService @Inject constructor(private val passwordHasher: BCryptPasswordHasher) {
    private val log: Logger = LoggerFactory.getLogger(UserService::class.java)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authorityRepository: AuthorityRepository

    fun activateRegistration(key: String): User? {
        log.debug("Activating user for activation key {}", key)
        val user = userRepository
            .findOneByActivationKey(key)

        if (user == null) {
            log.debug("No user was found for this activation key")
            return null
        }
        user.activated = true
        user.activationKey = "";
        clearUserCaches(user)
        log.debug("Activated user: {}", user)
        return user
    }

    fun changePassword(login: String, currentClearTextPassword: String, newPassword: String) {
        val user = userRepository.findOneByLogin(login)
            ?: throw IllegalArgumentException("No user was found for this login $login")

        val currentEncryptedPassword = user.password
        if (!passwordHasher.checkPassword(currentClearTextPassword, currentEncryptedPassword)) {
            throw InvalidPasswordException()
        }
        user.password = passwordHasher.hash(newPassword)
        clearUserCaches(user)
        log.debug("Changed password for User: {}", user)
    }

    fun completePasswordReset(newPassword: String, key: String): User {
        log.debug("Reset user password for reset key {}", key)
        val user = userRepository
            .findOneByResetKey(key)
            ?: throw AccountResourceException("No user was found for this reset key $key")

        if (user.resetDate?.isAfter(Instant.now().minusSeconds(86400)) == true) {
            user.password = passwordHasher.hash(newPassword)
            user.resetKey = ""
            user.resetDate = null
            clearUserCaches(user)
            log.debug("Changed password for User: {}", user)
            return user
        }
        throw IllegalArgumentException("Reset key has expired")
    }

    fun requestPasswordReset(mail: String): User {
        val user = userRepository.findOneByEmailIgnoreCase(mail)
            ?: throw EmailNotFoundException()

        if (!user.activated) {
            throw UserNotActivatedException("User is not activated")
        }
        user.resetKey = generateResetKey()
        user.resetDate = Instant.now()
        clearUserCaches(user)
        log.debug("Request to reset password for User: {}", user)
        return user
    }

    fun registerUser(userDTO: UserDTO, password: String): User {
        var existingUser =
            userRepository.findOneByLogin(userDTO.login.lowercase(Locale.getDefault()))
        var removed = existingUser?.let { removeNonActivatedUser(it) }
        if (!removed!!) {
            throw UsernameAlreadyUsedException()
        }
        existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.email)
        removed = existingUser?.let { removeNonActivatedUser(it) }
        if (!removed!!) {
            throw EmailAlreadyUsedException()
        }
        val newUser = User()
        newUser.login = userDTO.login.lowercase(Locale.getDefault())
        // new user gets initially a generated password
        newUser.password = passwordHasher.hash(password)
        newUser.firstName = userDTO.firstName
        newUser.lastName = userDTO.lastName
        newUser.email = userDTO.email.lowercase(Locale.getDefault())
        newUser.imageUrl = userDTO.imageUrl
        newUser.activated = false // new user is not active
        newUser.activationKey = generateActivationKey() // new user gets registration key
        val authorities = mutableSetOf<Authority>()
        val authority = authorityRepository.findByName(AuthoritiesConstants.USER)
        if (authority != null) {
            authorities.add(authority)
        }
        newUser.authorities = authorities
        userRepository.persist(newUser)
        clearUserCaches(newUser)
        log.debug("Created Information for User: {}", newUser)
        return newUser
    }

    private fun removeNonActivatedUser(existingUser: User): Boolean {
        if (existingUser.activated) {
            return false
        }
        userRepository.delete("id", existingUser.id)
        clearUserCaches(existingUser)
        return true
    }

    fun createUser(userDTO: UserDTO): User {
        val user: User = User()
        user.login = userDTO.login.lowercase(Locale.getDefault())
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email.lowercase(Locale.getDefault())
        user.imageUrl = userDTO.imageUrl
        user.langKey = userDTO.langKey
        user.password = passwordHasher.hash(generatePassword())
        user.resetKey = generateResetKey()
        user.resetDate = Instant.now()
        user.activated = true
        user.authorities = userDTO.authorities.stream().map { authority ->
            authorityRepository.findByName(authority)
        }.filter(Objects::nonNull).collect(Collectors.toSet())
        userRepository.persist(user)
        clearUserCaches(user)
        log.debug("Created Information for User: {}", user)
        return user
    }

    fun deleteUser(login: String) {
        val user = userRepository.findOneByLogin(login)
            ?: throw IllegalArgumentException("No user was found for this login $login")
        userRepository.delete("id", user.id)
        clearUserCaches(user)
        log.debug("Deleted User: {}", user)
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    fun updateUser(userDTO: UserDTO): UserDTO {
        val user = userRepository.findOneByLogin(userDTO.login.lowercase(Locale.getDefault()))
            ?: throw IllegalArgumentException("No user was found for this login ${userDTO.login}")

        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email.lowercase(Locale.getDefault())
        user.imageUrl = userDTO.imageUrl
        user.activated = userDTO.activated
        user.langKey = userDTO.langKey
        val managedAuthorities = user.authorities
        managedAuthorities.clear()
        userDTO.authorities.stream().map { authority ->
            authorityRepository.findByName(authority)
        }.filter(Objects::nonNull).forEach { auth ->
            if (auth != null) {
                managedAuthorities.add(auth)
            }
        }
        clearUserCaches(user)
        log.debug("Changed Information for User: {}", user)
        return UserDTO(user)
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param login     the login to find the user to update.
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    fun updateUser(
        login: String,
        firstName: String,
        lastName: String,
        email: String,
        langKey: String,
        imageUrl: String
    ) {
        val user = userRepository.findOneByLogin(login)
            ?: throw IllegalArgumentException("No user was found for this login $login")

        user.firstName = firstName
        user.lastName = lastName
        user.email = email.lowercase(Locale.getDefault())
        user.langKey = langKey
        user.imageUrl = imageUrl
        clearUserCaches(user)
        log.debug("Changed Information for User: {}", user)
    }

    fun getUserWithAuthoritiesByLogin(login: String): User? {
        return userRepository.findOneWithAuthoritiesByLogin(login)
    }

    fun getAllManagedUsers(): List<UserDTO> {
        return userRepository.findAllByLoginNot(Page.ofSize(20), Constants.ANONYMOUS_USER).stream()
            .map { UserDTO() }
            .collect(Collectors.toList())
    }
    fun getAuthorities(): List<String> {
        return authorityRepository.streamAll()
            .map { it.name }
            .collect(Collectors.toList())
    }

    private fun clearUserCaches(user: User) {
        clearUserCachesByLogin(user.login)
        clearUserCachesByEmail(user.email)
    }

    @CacheInvalidate(cacheName = User.USERS_BY_EMAIL_CACHE)
    fun clearUserCachesByEmail(email: String) {
        log.debug("Clearing cache for user by email $email")
    }

    @CacheInvalidate(cacheName = User.USERS_BY_LOGIN_CACHE)
    fun clearUserCachesByLogin(login: String) {
        log.debug("Clearing cache for user by login $login")
    }

    fun findOneByEmailIgnoreCase(email: String) = userRepository.findOneByEmailIgnoreCase(email)
    fun findOneByLogin(userLogin: String) = userRepository.findOneByLogin(userLogin)
}
