package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.security.AuthoritiesConstants
import com.acosta.quarkusastro.web.rest.vm.ConfigPropsVM
import com.acosta.quarkusastro.web.rest.vm.EnvVM
import io.quarkus.runtime.configuration.ProfileManager
import org.eclipse.microprofile.config.ConfigProvider
import org.eclipse.microprofile.config.spi.ConfigSource
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Path("/management")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
class ConfigurationEndpoint {
    @get:RolesAllowed(
        AuthoritiesConstants.ADMIN
    )
    @get:Path("/configprops")
    @get:GET
    val configs: ConfigPropsVM
        get() = ConfigPropsVM()

    @get:RolesAllowed(AuthoritiesConstants.ADMIN)
    @get:Path("/env")
    @get:GET
    val envs: EnvVM
        get() {
            val configSources = ConfigProvider.getConfig().configSources
            val propertySources: List<EnvVM.PropertySource> = StreamSupport.stream(configSources.spliterator(), false)
                .map { configSource: ConfigSource ->
                    EnvVM.PropertySource(
                        configSource.name,
                        configSource.properties
                    )
                }
                .collect(Collectors.toList())
            return EnvVM(listOf(ProfileManager.getActiveProfile()), propertySources)
        }
}
