package com.acosta.quarkusastro.security

import org.wildfly.security.credential.PasswordCredential
import org.wildfly.security.evidence.PasswordGuessEvidence
import org.wildfly.security.password.Password
import org.wildfly.security.password.PasswordFactory
import org.wildfly.security.password.WildFlyElytronPasswordProvider
import org.wildfly.security.password.interfaces.BCryptPassword
import org.wildfly.security.password.spec.EncryptablePasswordSpec
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec
import org.wildfly.security.password.util.ModularCrypt
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class BCryptPasswordHasher @JvmOverloads constructor(
    private val iterationCount: Int = DEFAULT_ITERATION_COUNT,
    private val random: SecureRandom? = null
) {
    fun checkPassword(plaintextPassword: String, hashedPassword: String): Boolean {
        Objects.requireNonNull(plaintextPassword, "plaintext password is required")
        Objects.requireNonNull(hashedPassword, "hashed password is required")
        val evidence = PasswordGuessEvidence(plaintextPassword.toCharArray())
        val credential = PasswordCredential(decode(hashedPassword))
        return credential.verify(evidence)
    }

    private fun decode(password: String): Password {
        return try {
            ModularCrypt.decode(password)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException(e)
        }
    }

    fun hash(password: String): String {
        Objects.requireNonNull(password, "password is required")
        require(iterationCount > 0) { "Iteration count must be greater than zero" }
        val salt = ByteArray(BCryptPassword.BCRYPT_SALT_SIZE)
        if (random != null) {
            random.nextBytes(salt)
        } else {
            SecureRandom().nextBytes(salt)
        }
        val passwordFactory: PasswordFactory = try {
            PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, provider)
        } catch (e: NoSuchAlgorithmException) {
            // can't really happen
            throw RuntimeException(e)
        }
        val iteratedAlgorithmSpec = IteratedSaltedPasswordAlgorithmSpec(
            iterationCount, salt
        )
        val encryptableSpec = EncryptablePasswordSpec(password.toCharArray(), iteratedAlgorithmSpec)
        return try {
            val original = passwordFactory.generatePassword(encryptableSpec) as BCryptPassword
            ModularCrypt.encodeAsString(original)
        } catch (e: InvalidKeySpecException) {
            // can't really happen
            throw RuntimeException(e)
        }
    }

    companion object {
        private val provider = WildFlyElytronPasswordProvider()
        const val DEFAULT_ITERATION_COUNT = 10
    }
}
