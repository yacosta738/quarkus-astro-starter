package com.acosta.quarkusastro.web.rest.vm

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class KeyAndPasswordVM(
    val key: String,
    val newPassword: String
) {
    override fun toString(): String {
        return "KeyAndPasswordVM{key='$key', newPassword='$newPassword'}"
    }
}