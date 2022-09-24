package com.acosta.quarkusastro.security

import java.security.SecureRandom


object RandomUtil {
    private const val DEF_COUNT = 20
    private var SECURE_RANDOM: SecureRandom = SecureRandom()

    init {
        SECURE_RANDOM.nextBytes(ByteArray(64))
    }

    private fun generateRandomAlphanumericString(): String {
        return org.apache.commons.lang3.RandomStringUtils.random(
            DEF_COUNT,
            0,
            0,
            true,
            true,
            null,
            SECURE_RANDOM
        )
    }

    /**
     * Generate a password.
     *
     * @return the generated password.
     */
    fun generatePassword(): String {
        return generateRandomAlphanumericString()
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key.
     */
    fun generateActivationKey(): String {
        return generateRandomAlphanumericString()
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key.
     */
    fun generateResetKey(): String {
        return generateRandomAlphanumericString()
    }
}
