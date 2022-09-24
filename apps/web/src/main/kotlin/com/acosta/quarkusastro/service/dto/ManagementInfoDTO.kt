package com.acosta.quarkusastro.service.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import javax.json.bind.annotation.JsonbProperty


/**
 * DTO to emulate /management/info response
 */
@RegisterForReflection
class ManagementInfoDTO {
    var activeProfiles: MutableList<String> = ArrayList()

    @JsonbProperty("display-ribbon-on-profiles")
    var displayRibbonOnProfiles: String? = null
}
