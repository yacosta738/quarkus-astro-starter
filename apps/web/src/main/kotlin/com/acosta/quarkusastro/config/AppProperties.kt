package com.acosta.quarkusastro.config

import io.smallrye.config.ConfigMapping


@ConfigMapping(prefix = "app")
class AppProperties {
    lateinit var security: Security
    lateinit var mail: Mail

    class Security {
        lateinit var authentication: Authentication

        class Authentication {
            lateinit var jwt: Jwt

            class Jwt {
                var issuer: String? = null
                var tokenValidityInSeconds: Long = 0
                var tokenValidityInSecondsForRememberMe: Long = 0
                var privateKey: PrivateKey? = null

                class PrivateKey {
                    lateinit var location: String
                }
            }
        }
    }

    class Mail {
        lateinit var baseUrl: String
    }
}
