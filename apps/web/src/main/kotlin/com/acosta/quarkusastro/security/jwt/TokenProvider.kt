package com.acosta.quarkusastro.security.jwt

import com.acosta.quarkusastro.config.AppProperties
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.lang.JoseException
import org.slf4j.LoggerFactory
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
class TokenProvider @Inject constructor(appProperties: AppProperties) {
    private val log = LoggerFactory.getLogger(TokenProvider::class.java)
    private val key: Key
    private val issuer: String
    private val tokenValidityInMilliseconds: Long
    private val tokenValidityInMillisecondsForRememberMe: Long

    init {
        key = readPrivateKey(
            appProperties.security().authentication().jwt().privateKey().location()
        )
        issuer = appProperties.security().authentication().jwt().issuer().toString()
        tokenValidityInMilliseconds =
            appProperties.security().authentication().jwt().tokenValidityInSeconds() * 1000
        tokenValidityInMillisecondsForRememberMe =
            appProperties.security().authentication().jwt()
                .tokenValidityInSecondsForRememberMe() * 1000
    }

    fun createToken(identity: QuarkusSecurityIdentity, rememberMe: Boolean): String {
        val authorities = java.lang.String.join(", ", identity.roles)
        val now = Date().time
        val validity: Date = if (rememberMe) {
            Date(now + tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + tokenValidityInMilliseconds)
        }
        val claims = JwtClaims()
        claims.subject = identity.principal.name
        claims.setClaim(AUTHORITIES_KEY, authorities)
        claims.setClaim(GROUPS_KEY, identity.roles)
        claims.issuedAt = NumericDate.fromMilliseconds(now)
        claims.issuer = issuer
        claims.expirationTime = NumericDate.fromMilliseconds(validity.time)

        val jws = JsonWebSignature()
        jws.payload = claims.toJson()
        jws.key = key
        jws.keyIdHeaderValue = UUID.randomUUID().toString()
        jws.setHeader("typ", "JWT")
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        return try {
            jws.compactSerialization
        } catch (e: JoseException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val AUTHORITIES_KEY = "auth" // Claim front-end uses
        const val GROUPS_KEY = "groups" // Default claim for MP-JWT
        @Throws(Exception::class)
        fun readPrivateKey(pemResName: String): PrivateKey {
            val contentIS = TokenProvider::class.java.getResourceAsStream(pemResName)
            val tmp = ByteArray(4096)
            val length = contentIS?.read(tmp)
            val pem = String(tmp, 0, length!!, Charsets.UTF_8)
            return decodePrivateKey(pem)
        }

        @Throws(Exception::class)
        fun decodePrivateKey(pemEncoded: String): PrivateKey {
            val encodedBytes = toEncodedBytes(pemEncoded)
            val keySpec = PKCS8EncodedKeySpec(encodedBytes)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePrivate(keySpec)
        }

        private fun toEncodedBytes(pemEncoded: String): ByteArray {
            val normalizedPem = removeBeginEnd(pemEncoded)
            return Base64.getDecoder().decode(normalizedPem)
        }

        private fun removeBeginEnd(pem: String): String {
            var pemKey = pem
            pemKey = pemKey.replace("-----BEGIN (.*)-----".toRegex(), "")
            pemKey = pemKey.replace("-----END (.*)----".toRegex(), "")
            pemKey = pemKey.replace("\r\n".toRegex(), "")
            pemKey = pemKey.replace("\n".toRegex(), "")
            return pemKey.trim { it <= ' ' }
        }
    }
}
