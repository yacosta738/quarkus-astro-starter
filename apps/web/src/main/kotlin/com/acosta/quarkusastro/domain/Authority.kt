package com.acosta.quarkusastro.domain

import io.quarkus.runtime.annotations.RegisterForReflection
import java.io.Serializable
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


/**
 * An authority (a security role).
 */
@Entity
@Table(name = "authorities")
@Cacheable
@RegisterForReflection
open class Authority: Serializable {
    @Id
    @Column(length = 50)
    var name: @NotNull @Size(max = 50) String = ""

    constructor() {
        //empty
    }

    constructor(name: String) {
        //for jsonb
        this.name = name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        return if (o !is Authority) {
            false
        } else name == o.name
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun toString(): String {
        return "Authority{name='$name'}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
