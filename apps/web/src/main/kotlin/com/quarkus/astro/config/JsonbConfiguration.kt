package com.quarkus.astro.config

import io.quarkus.jsonb.JsonbConfigCustomizer
import java.util.*
import javax.inject.Singleton
import javax.json.bind.JsonbConfig


/**
 * Jsonb Configuration
 * Further details https://quarkus.io/guides/rest-json#configuring-json-support
 */
@Singleton
class JsonbConfiguration : JsonbConfigCustomizer {
    override fun customize(config: JsonbConfig) {
        config
            .withDateFormat(com.quarkus.astro.config.Constants.DATE_TIME_FORMAT, Locale.getDefault())
    }
}
