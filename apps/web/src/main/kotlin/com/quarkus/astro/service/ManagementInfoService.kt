package com.quarkus.astro.service

import com.quarkus.astro.config.AppProperties
import com.quarkus.astro.service.dto.ManagementInfoDTO
import io.quarkus.runtime.configuration.ProfileManager
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


/**
 * Provides information for management/info resource
 */
@ApplicationScoped
class ManagementInfoService @Inject constructor(private val info: com.quarkus.astro.config.AppProperties) {

    val managementInfo: ManagementInfoDTO
        get() {
            val info = ManagementInfoDTO()
            if (this.info.info().swagger().enable()) {
                info.activeProfiles.add("swagger")
            }
            info.activeProfiles.add(ProfileManager.getActiveProfile())
            info.displayRibbonOnProfiles = ProfileManager.getActiveProfile()
            return info
        }
}
