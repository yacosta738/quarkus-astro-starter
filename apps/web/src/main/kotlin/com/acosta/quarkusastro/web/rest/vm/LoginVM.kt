package com.acosta.quarkusastro.web.rest.vm

import io.quarkus.runtime.annotations.RegisterForReflection
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


/**
 * View Model object for storing a user's credentials.
 */
@RegisterForReflection
class LoginVM {
    lateinit var username: @NotNull @Size(min = 1, max = 50) String
    lateinit var password: @NotNull @Size(min = 4, max = 100) String
    var rememberMe: Boolean = false
    override fun toString(): String {
        return "LoginVM{username='$username', rememberMe=$rememberMe}"
    }
}
