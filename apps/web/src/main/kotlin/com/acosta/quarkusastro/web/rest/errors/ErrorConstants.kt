package com.acosta.quarkusastro.web.rest.errors

import java.net.URI

object ErrorConstants {
    const val PROBLEM_BASE_URL = "https://www.app.com/problem"
    val DEFAULT_TYPE: URI = URI.create("$PROBLEM_BASE_URL/problem-with-message")
}
