package com.acosta.quarkusastro.config

import io.smallrye.config.ConfigMapping
import org.eclipse.microprofile.config.inject.ConfigProperty


@ConfigMapping(prefix = "app.info")
interface AppInfo {
    @get:ConfigProperty(
        name = "swagger.enable",
        defaultValue = "true"
    )
    val isEnable: Boolean
}
