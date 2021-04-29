package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/attributes")
@Produces(MediaType.APPLICATION_JSON)
class AttributeResource(override val registry: Registry) : ModelResource {

    override val endpoint: String = "/attributes"

    @GET
    @Path("/query/{identifier}")
    fun query(
        @PathParam("identifier") identifier: String,
        @QueryParam("name") name: String
    ): Uni<Response> = forward("/query", identifier) {
        addQueryParam("name", name)
    }

    @GET
    @Path("/deprecated/{identifier}")
    fun deprecated(
        @PathParam("identifier") identifier: String,
        @QueryParam("since") @DefaultValue("") since: String = ""
    ): Uni<Response> = forward("/deprecated", identifier) {
        addQueryParam("since", since)
    }
}
