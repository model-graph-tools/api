package org.wildfly.modelgraph.model

import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/capabilities")
@Produces(MediaType.APPLICATION_JSON)
class CapabilityResource(
    override val registry: Registry,
    override val config: Config
) : ModelResource {

    override val endpoint: String = "/capabilities"

    @GET
    @Path("/query/{identifier}")
    suspend fun query(
        @PathParam("identifier") identifier: String,
        @QueryParam("name") name: String
    ): Response = forward("/query", identifier) {
        addQueryParam("name", name)
    }
}
