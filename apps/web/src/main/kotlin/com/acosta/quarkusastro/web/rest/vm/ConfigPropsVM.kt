package com.acosta.quarkusastro.web.rest.vm

import io.quarkus.runtime.annotations.RegisterForReflection


@RegisterForReflection
class ConfigPropsVM {
    val contexts: MutableMap<String, Context> = HashMap()

    init {
        contexts["App"] = Context()
    }

    @RegisterForReflection
    inner class Context {
        val beans: Map<String, Any> = HashMap()
    }
}