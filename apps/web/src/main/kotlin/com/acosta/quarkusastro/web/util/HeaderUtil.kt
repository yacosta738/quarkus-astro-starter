package com.acosta.quarkusastro.web.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object HeaderUtil {
    private val log: Logger = LoggerFactory.getLogger(HeaderUtil::class.java)

    /**
     *
     * createAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param message         a [java.lang.String] object.
     * @param param           a [java.lang.String] object.
     * @return a [java.util.Map] object.
     */
    fun createAlert(applicationName: String, message: String, param: String): Map<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        headers["X-$applicationName-alert"] = message
        headers["X-$applicationName-params"] = param
        return headers
    }

    /**
     *
     * createEntityCreationAlert.
     *
     * @param applicationName   a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [java.lang.String] object.
     * @param param             a [java.lang.String] object.
     * @return a [javax.ws.rs.core.MultivaluedMap] object.
     */
    fun createEntityCreationAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): Map<String, String> {
        val message =
            if (enableTranslation) "$applicationName.$entityName.created" else "A new $entityName is created with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityUpdateAlert.
     *
     * @param applicationName   a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [java.lang.String] object.
     * @param param             a [java.lang.String] object.
     * @return a [javax.ws.rs.core.MultivaluedMap] object.
     */
    fun createEntityUpdateAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): Map<String, String> {
        val message =
            if (enableTranslation) "$applicationName.$entityName.updated" else "A $entityName is updated with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityDeletionAlert.
     *
     * @param applicationName   a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [java.lang.String] object.
     * @param param             a [java.lang.String] object.
     * @return a [javax.ws.rs.core.MultivaluedMap] object.
     */
    fun createEntityDeletionAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): Map<String, String> {
        val message =
            if (enableTranslation) "$applicationName.$entityName.deleted" else "A $entityName is deleted with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createFailureAlert.
     *
     * @param applicationName   a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [java.lang.String] object.
     * @param errorKey          a [java.lang.String] object.
     * @param defaultMessage    a [java.lang.String] object.
     * @return a [javax.ws.rs.core.MultivaluedMap] object.
     */
    fun createFailureAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        errorKey: String,
        defaultMessage: String?
    ): Map<String, String> {
        log.error("Entity processing failed, {}", defaultMessage)
        val message = if (enableTranslation) "error.$errorKey" else defaultMessage!!
        val headers: MutableMap<String, String> = HashMap()
        headers["X-$applicationName-error"] = message
        headers["X-$applicationName-params"] = entityName
        return headers
    }
}
