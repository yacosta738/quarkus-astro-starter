package com.acosta.quarkusastro.repository

import com.acosta.quarkusastro.domain.Authority
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AuthorityRepository: PanacheRepository<Authority> {
    fun findByName(name: String) = find("name", name).firstResult()
}