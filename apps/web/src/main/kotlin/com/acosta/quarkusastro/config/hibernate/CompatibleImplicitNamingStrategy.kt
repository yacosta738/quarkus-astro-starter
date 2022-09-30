package com.acosta.quarkusastro.config.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl


open class CompatibleImplicitNamingStrategy :
    ImplicitNamingStrategyJpaCompliantImpl() {
    override fun determineJoinTableName(source: ImplicitJoinTableNameSource): Identifier {
        val joinedName =
            "${source.owningPhysicalTableName}_${source.associationOwningAttributePath.property}"
        return toIdentifier(joinedName, source.buildingContext)
    }
}
