package com.acosta.quarkusastro.config

/**
 * Application constants.
 */
object Constants {
    // Regex for acceptable logins
    const val LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$"
    const val SYSTEM_ACCOUNT = "system"
    const val ANONYMOUS_USER = "anonymoususer"
    const val DEFAULT_LANGUAGE = "en"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val LOCAL_DATE_FORMAT = "yyyy-MM-dd"
}
