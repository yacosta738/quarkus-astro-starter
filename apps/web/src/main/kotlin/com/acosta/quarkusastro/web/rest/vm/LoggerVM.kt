package com.acosta.quarkusastro.web.rest.vm

import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.logging.Logger


@RegisterForReflection
class LoggerVM(
    var name: String = "",
    var effectiveLevel: String = "",
    var configuredLevel: String = ""
) {
    constructor(name: String, logger: Logger) : this() {
        this.name = name
        this.effectiveLevel = logger.level.name
        this.configuredLevel = retrieveEffectiveLogLevel(logger)
    }

    private fun retrieveEffectiveLogLevel(logger: Logger): String {
        return if (logger.level != null) {
            logger.level.name
        } else retrieveEffectiveLogLevel(logger.parent)
    }
}