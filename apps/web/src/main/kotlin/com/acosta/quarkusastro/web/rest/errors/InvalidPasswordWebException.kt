package com.acosta.quarkusastro.web.rest.errors

import java.net.URI


class InvalidPasswordWebException :
    BadRequestAlertException(TYPE, "Incorrect Password!", "userManagement", "incorrectpassword") {
        companion object{
            val TYPE: URI = URI.create(ErrorConstants.PROBLEM_BASE_URL + "/invalid-password")
        }
}
