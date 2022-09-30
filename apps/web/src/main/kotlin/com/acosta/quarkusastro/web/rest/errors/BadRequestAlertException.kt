package com.acosta.quarkusastro.web.rest.errors

import com.tietoevry.quarkus.resteasy.problem.HttpProblem
import org.eclipse.microprofile.config.ConfigProvider
import java.net.URI
import javax.ws.rs.core.Response


open class BadRequestAlertException(
    type: URI,
    defaultMessage: String,
    entityName: String,
    errorKey: String
) :
    HttpProblem(
        builder()
            .withType(type)
            .withStatus(Response.Status.BAD_REQUEST)
            .withTitle(defaultMessage)
            .withHeader(
                "X-$APPLICATION_NAME-error",
                "error.$errorKey"
            )
            .withHeader("X-$APPLICATION_NAME-params", entityName)
            .with("entityName", entityName)
            .with("errorKey", errorKey)
            .with("message", "error.$errorKey")
            .with("params", entityName)
    ) {
    constructor(defaultMessage: String, entityName: String, errorKey: String) : this(
        ErrorConstants.DEFAULT_TYPE,
        defaultMessage,
        entityName,
        errorKey
    ) {
    }

    companion object {
        private val APPLICATION_NAME = ConfigProvider.getConfig()
            .getOptionalValue("application.name", String::class.java)
            .orElse("Astro Quarkus")
        private const val serialVersionUID = 1L
    }
}