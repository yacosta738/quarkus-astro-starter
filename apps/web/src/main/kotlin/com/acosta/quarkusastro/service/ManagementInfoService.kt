package com.acosta.quarkusastro.service

import com.acosta.quarkusastro.config.AppProperties
import com.acosta.quarkusastro.service.dto.ManagementInfoDTO
import io.quarkus.runtime.configuration.ProfileManager
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


/**
 * Provides information for management/info resource
 */
@ApplicationScoped
class ManagementInfoService @Inject constructor(private val info: AppProperties) {

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
