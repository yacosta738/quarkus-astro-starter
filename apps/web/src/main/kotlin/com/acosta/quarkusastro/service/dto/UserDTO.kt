package com.acosta.quarkusastro.service.dto

import com.acosta.quarkusastro.config.Constants
import com.acosta.quarkusastro.domain.User
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant
import java.util.stream.Collectors
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


/**
 * A DTO representing a user, with his authorities.
 */
@RegisterForReflection
open class UserDTO {
    var id: Long? = null
    var login: @NotBlank @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String =
        ""
    var firstName: @Size(max = 50) String = ""
    var lastName: @Size(max = 50) String = ""
    var email: @Email @Size(min = 5, max = 254) String = ""
    var imageUrl: @Size(max = 256) String = ""
    var activated = false
    var langKey: @Size(min = 2, max = 10) String = "en"
    lateinit var createdBy: String
    var createdDate: Instant = Instant.now()
    lateinit var lastModifiedBy: String
    lateinit var lastModifiedDate: Instant
    lateinit var authorities: Set<String>

    constructor() {
        // Empty constructor needed for Jackson.
    }

    constructor(user: User) {
        id = user.id
        login = user.login
        firstName = user.firstName
        lastName = user.lastName
        email = user.email
        activated = user.activated
        imageUrl = user.imageUrl
        langKey = user.langKey
        createdBy = user.createdBy
        createdDate = user.createdDate
        lastModifiedBy = user.lastModifiedBy
        lastModifiedDate = user.lastModifiedDate
        authorities = user.authorities.stream().map { it.name }.collect(Collectors.toSet()) as Set<String>
    }

    override fun toString(): String {
        return "UserDTO{" +
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
                ", activated=" +
                activated +
                ", langKey='" +
                langKey +
                '\'' +
                ", createdBy=" +
                createdBy +
                ", createdDate=" +
                createdDate +
                ", lastModifiedBy='" +
                lastModifiedBy +
                '\'' +
                ", lastModifiedDate=" +
                lastModifiedDate +
                ", authorities=" +
                authorities +
                "}"
    }
}
