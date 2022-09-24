package com.acosta.quarkusastro.domain

import com.acosta.quarkusastro.config.Constants
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.panache.common.Page
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.json.bind.annotation.JsonbTransient
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


/**
 * A user.
 */
@Entity
@Table(name = "user")
@Cacheable
class User : PanacheEntityBase(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(length = 50, unique = true, nullable = false)
    var login: @NotNull @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String? =
        null

    @Column(name = "password_hash", length = 60, nullable = false)
    @JsonbTransient
    var password: @NotNull @Size(min = 60, max = 60) String? = null

    @Column(name = "first_name", length = 50)
    var firstName: @Size(max = 50) String? = null

    @Column(name = "last_name", length = 50)
    var lastName: @Size(max = 50) String? = null

    @Column(length = 254, unique = true)
    var email: @Email @Size(min = 5, max = 254) String? = null

    @Column(nullable = false)
    var activated: @NotNull Boolean = false

    @Column(name = "lang_key", length = 10)
    var langKey: @Size(min = 2, max = 10) String? = null

    @Column(name = "image_url", length = 256)
    var imageUrl: @Size(max = 256) String? = null

    @Column(name = "activation_key", length = 20)
    @JsonbTransient
    var activationKey: @Size(max = 20) String? = null

    @Column(name = "reset_key", length = 20)
    @JsonbTransient
    var resetKey: @Size(max = 20) String? = null

    @Column(name = "reset_date")
    var resetDate: Instant? = null

    @ManyToMany
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_name", referencedColumnName = "name")]
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @BatchSize(size = 20)
    @JsonbTransient
    var authorities: Set<Authority> = HashSet()

    //To move to an audit mechanism
    //    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    @JsonbTransient
    var createdBy = ""

    //    @CreatedDate
    @Column(name = "created_date", updatable = false)
    @JsonbTransient
    var createdDate = Instant.now()

    //    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    @JsonbTransient
    var lastModifiedBy = ""

    //    @LastModifiedDate
    @Column(name = "last_modified_date")
    @JsonbTransient
    var lastModifiedDate = Instant.now()


    override fun toString(): String {
        return "User{" +
                "login='" +
                login +
                '\'' +
                ", firstName='" +
                firstName +
                '\'' +
                ", lastName='" +
                lastName +
                '\'' +
                ", email='" +
                email +
                '\'' +
                ", imageUrl='" +
                imageUrl +
                '\'' +
                ", activated='" +
                activated +
                '\'' +
                ", langKey='" +
                langKey +
                '\'' +
                ", activationKey='" +
                activationKey +
                '\'' +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {
        private const val serialVersionUID = 1L
        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
        const val USERS_BY_LOGIN_CACHE = "usersByLogin"
        fun findOneByActivationKey(activationKey: String): Optional<User> {
            return find<User>(
                "activationKey",
                activationKey
            ).firstResultOptional()
        }

        fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(dateTime: Instant): List<User> {
            return list(
                "activated = false and activationKey not null and createdDate <= ?1",
                dateTime
            )
        }

        fun findOneByResetKey(resetKey: String): Optional<User> {
            return find<User>(
                "resetKey",
                resetKey
            ).firstResultOptional()
        }

        fun findOneByEmailIgnoreCase(email: String): Optional<User> {
            return find<User>(
                "LOWER(email) = LOWER(?1)",
                email
            ).firstResultOptional()
        }

        fun findOneByLogin(login: String): Optional<User> {
            return find<User>("login", login).firstResultOptional()
        }

        fun findOneWithAuthoritiesById(id: Long): Optional<User> {
            return find<User>(
                "FROM User u LEFT JOIN FETCH u.authorities WHERE u.id = ?1",
                id
            ).firstResultOptional()
        }

        @CacheResult(cacheName = USERS_BY_LOGIN_CACHE)
        fun findOneWithAuthoritiesByLogin(login: String): Optional<User> {
            return find<User>(
                "FROM User u LEFT JOIN FETCH u.authorities WHERE u.login = ?1",
                login
            )
                .firstResultOptional()
        }

        @CacheResult(cacheName = USERS_BY_EMAIL_CACHE)
        fun findOneWithAuthoritiesByEmailIgnoreCase(email: String): Optional<User> {
            return find<User>(
                "FROM User u LEFT JOIN FETCH u.authorities WHERE LOWER(u.login) = LOWER(?1)",
                email
            )
                .firstResultOptional()
        }

        fun findAllByLoginNot(page: Page, login: String): List<User> {
            return find<User>("login != ?1", login).page<User>(page)
                .list()
        }
    }
}

