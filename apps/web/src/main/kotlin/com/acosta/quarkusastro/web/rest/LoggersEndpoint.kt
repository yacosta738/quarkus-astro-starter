package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.security.AuthoritiesConstants
import com.acosta.quarkusastro.web.rest.vm.LoggerVM
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*
import java.util.function.Function
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/management/loggers")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
class LoggersEndpoint {
    @get:RolesAllowed(AuthoritiesConstants.ADMIN)
    @get:GET
    val loggers: LoggersWrapper
        get() {
            val loggerNames = LogManager.getLogManager().loggerNames
            val loggers: Map<String, LoggerVM> = Collections
                .list(loggerNames)
                .stream()
                .filter { name: String -> name.isNotBlank() }
                .map { name: String -> this.getLogger(name) }
                .collect(Collectors.toMap(LoggerVM::name, Function.identity()))

            return LoggersWrapper(loggers)
        }

    private fun getLogger(loggerName: String): LoggerVM {
        val logger = LogManager.getLogManager().getLogger(loggerName)
        return LoggerVM(logger.name, logger)
    }

    @POST
    @Path("/{name}")
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    fun updateLoggerLevel(@PathParam("name") name: String, loggerVM: LoggerVM): Response {
        val logger = Logger.getLogger(name)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val level = Level.parse(loggerVM.configuredLevel)
        logger.level = level
        return Response.ok().build()
    }

    @RegisterForReflection
    class LoggersWrapper(loggers: Map<String, LoggerVM>) {
        private val loggers: Map<String, LoggerVM>

        init {
            this.loggers = loggers
        }
    }
}
