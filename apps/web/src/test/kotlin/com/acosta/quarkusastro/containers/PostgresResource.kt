package com.acosta.quarkusastro.containers

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*


class PostgresResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        db.start()
        return mapOf(
            "quarkus.datasource.jdbc.url" to db.jdbcUrl,
            "quarkus.datasource.username" to db.username,
            "quarkus.datasource.password" to db.password
        )
    }

    override fun stop() {
        db.stop()
    }

    companion object {
        var db: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14")
            .withDatabaseName("quarkusastro")
            .withUsername("acosta")
            .withPassword("secret-password")
    }
}
