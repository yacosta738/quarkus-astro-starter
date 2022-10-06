package com.quarkus.astro.repository

import com.quarkus.astro.domain.Authority
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AuthorityRepository: PanacheRepository<Authority> {
    fun findByName(name: String) = find("name", name).firstResult()
}
