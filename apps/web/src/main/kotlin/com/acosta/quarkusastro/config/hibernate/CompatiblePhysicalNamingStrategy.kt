package com.acosta.quarkusastro.config.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment


open class CompatiblePhysicalNamingStrategy : PhysicalNamingStrategy {
    override fun toPhysicalCatalogName(
        name: Identifier,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return apply(name, jdbcEnvironment)
    }

    override fun toPhysicalSchemaName(
        name: Identifier,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return apply(name, jdbcEnvironment)
    }

    override fun toPhysicalTableName(
        name: Identifier,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return apply(name, jdbcEnvironment)
    }

    override fun toPhysicalSequenceName(
        name: Identifier,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return apply(name, jdbcEnvironment)
    }

    override fun toPhysicalColumnName(
        name: Identifier,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return apply(name, jdbcEnvironment)
    }

    protected fun getIdentifier(
        name: String,
        quoted: Boolean,
        jdbcEnvironment: JdbcEnvironment
    ): Identifier {
        return Identifier(name.lowercase(), quoted)
    }

    private fun isUnderscoreRequired(before: Char, current: Char, after: Char): Boolean {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(
            after
        )
    }

    private fun apply(name: Identifier, jdbcEnvironment: JdbcEnvironment): Identifier {
        val builder = StringBuilder(name.text.replace('.', '_'))
        var i = 1
        while (i < builder.length - 1) {
            if (isUnderscoreRequired(builder[i - 1], builder[i], builder[i + 1])) {
                builder.insert(i++, '_')
            }
            i++
        }
        return getIdentifier(builder.toString(), name.isQuoted, jdbcEnvironment)
    }
}
