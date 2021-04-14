package org.wildfly.modelgraph.registry

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource(val registry: Registry) {

    @GET
    fun list(): List<Version> = registry.modelServices.keys.toList()
}
