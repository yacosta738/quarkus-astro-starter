package com.acosta.quarkusastro.web.rest.vm

import com.acosta.quarkusastro.service.dto.UserDTO
import io.quarkus.runtime.annotations.RegisterForReflection
import javax.validation.constraints.Size


/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
@RegisterForReflection
class ManagedUserVM : UserDTO() {
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    lateinit var password: String
    override fun toString(): String {
        return "ManagedUserVM{" + super.toString() + "} "
    }

    companion object {
        const val PASSWORD_MIN_LENGTH = 4
        const val PASSWORD_MAX_LENGTH = 100
    }
}
