package com.acosta.quarkusastro.web.rest.vm

import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*
import java.util.stream.Collectors


@RegisterForReflection
class EnvVM(
    val activeProfiles: Collection<String>,
    val propertySources: Collection<PropertySource>
) {
    @RegisterForReflection
    class PropertySource(val name: String, properties: Map<String, String>) {
        val properties: Map<String, PropertyValue>

        init {
            this.properties = properties.entries.stream().collect(
                Collectors.toMap(
                    { e: Map.Entry<String, String> -> e.key },
                    { e: Map.Entry<String, String> -> PropertyValue(e.value) }
                )
            )
        }


        @RegisterForReflection
        class PropertyValue {
            val value: String

            constructor(entry: Map.Entry<String, String>) {
                value = retrieveValue(entry)
            }

            constructor(value: String) {
                this.value = value
            }

            private fun retrieveValue(entry: Map.Entry<String, String>): String {
                val obfuscatedValue = "******"
                val prefixes = arrayOf("password", "credentials", "credential", "secret", "secrets")
                for (prefix in prefixes) {
                    val key = entry.key
                    if (key.lowercase(Locale.getDefault()).contains(prefix)) {
                        return obfuscatedValue
                    }
                }
                return entry.value
            }
        }
    }
}
