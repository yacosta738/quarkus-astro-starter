package com.acosta.quarkusastro.config

import io.smallrye.config.ConfigMapping


@ConfigMapping(prefix = "app")
interface AppProperties {
    fun info(): Info

    interface Info {
        fun swagger(): Swagger
        interface Swagger {
            fun enable(): Boolean
        }
    }

    fun security(): Security
    fun mail(): Mail

    interface Security {
        fun authentication(): Authentication

        interface Authentication {
            fun jwt(): Jwt

            interface Jwt {
                fun issuer(): String
                fun tokenValidityInSeconds(): Long
                fun tokenValidityInSecondsForRememberMe(): Long
                fun privateKey(): PrivateKey

                interface PrivateKey {
                    fun location(): String
                }
            }
        }
    }

    interface Mail {
        fun baseUrl(): String
    }
}
