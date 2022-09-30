package com.acosta.quarkusastro.domain

import com.acosta.quarkusastro.config.Constants
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.time.Instant
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
@Table(name = "users")
@Cacheable
open class User :  Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(length = 50, unique = true, nullable = false)
    lateinit var login: @NotNull @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String

    @Column(name = "password_hash", length = 60, nullable = false)
    @JsonbTransient
    lateinit var password: @NotNull @Size(min = 60, max = 60) String

    @Column(name = "first_name", length = 50)
    lateinit var firstName: @Size(max = 50) String

    @Column(name = "last_name", length = 50)
    lateinit var lastName: @Size(max = 50) String

    @Column(length = 254, unique = true)
    lateinit var email: @Email @Size(min = 5, max = 254) String

    @Column(nullable = false)
    var activated: @NotNull Boolean = false

    @Column(name = "lang_key", length = 10)
    lateinit var langKey: @Size(min = 2, max = 10) String

    @Column(name = "image_url", length = 256)
    lateinit var imageUrl: @Size(max = 256) String

    @Column(name = "activation_key", length = 20)
    @JsonbTransient
    lateinit var activationKey: @Size(max = 20) String

    @Column(name = "reset_key", length = 20)
    @JsonbTransient
    lateinit var resetKey: @Size(max = 20) String

    @Column(name = "reset_date")
    var resetDate: Instant? = null

    @ManyToMany
    @JoinTable(
        name = "user_authority",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_name", referencedColumnName = "name")]
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @BatchSize(size = 20)
    @JsonbTransient
    var authorities: MutableSet<Authority> = HashSet()

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
        return id.hashCode() + 31
    }

    companion object{
        private const val serialVersionUID = 1L
        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
        const val USERS_BY_LOGIN_CACHE = "usersByLogin"
    }
}

