package com.acosta.quarkusastro.web.rest

import com.acosta.quarkusastro.service.ManagementInfoService
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/management/info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
class ManagementInfoResource @Inject constructor(private val managementInfoService: ManagementInfoService) {
    @GET
    fun info(): Response {
        return Response.ok(managementInfoService.managementInfo).build()
    }
}
