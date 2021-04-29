package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource(override val registry: Registry) : ModelResource {

    override val endpoint: String = "/versions"

    @GET
    @Path("/{identifier}")
    fun versions(@PathParam("identifier") identifier: String): Uni<Response> = forward("/", identifier)
}