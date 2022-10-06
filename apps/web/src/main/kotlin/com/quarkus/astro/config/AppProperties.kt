package com.quarkus.astro.config

import io.smallrye.config.ConfigMapping


@ConfigMapping(prefix = "app")
interface AppProperties {
    fun info(): com.quarkus.astro.config.AppProperties.Info

    interface Info {
        fun swagger(): com.quarkus.astro.config.AppProperties.Info.Swagger
        interface Swagger {
            fun enable(): Boolean
        }
    }

    fun security(): com.quarkus.astro.config.AppProperties.Security
    fun mail(): com.quarkus.astro.config.AppProperties.Mail

    interface Security {
        fun authentication(): com.quarkus.astro.config.AppProperties.Security.Authentication

        interface Authentication {
            fun jwt(): com.quarkus.astro.config.AppProperties.Security.Authentication.Jwt

            interface Jwt {
                fun issuer(): String
                fun tokenValidityInSeconds(): Long
                fun tokenValidityInSecondsForRememberMe(): Long
                fun privateKey(): com.quarkus.astro.config.AppProperties.Security.Authentication.Jwt.PrivateKey

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
