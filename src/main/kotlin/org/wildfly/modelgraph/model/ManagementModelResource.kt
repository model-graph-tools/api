package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/management-model")
@Produces(MediaType.APPLICATION_JSON)
class ManagementModelResource(override val registry: Registry) : ModelResource {

    override val endpoint: String = "/management-model"

    @GET
    @Path("/query/{version}")
    fun query(
        @PathParam("version") version: String,
        @QueryParam("name") name: String
    ): Uni<Response> = forward("/query", version) {
        addQueryParam("name", name)
    }

    @GET
    @Path("/deprecated/{version}")
    fun deprecated(
        @PathParam("version") version: String,
        @QueryParam("since") since: String
    ): Uni<Response> = forward("/deprecated", version) {
        addQueryParam("since", since)
    }

    @GET
    @Path("/versions/{version}")
    fun versions(@PathParam("version") version: String): Uni<Response> = forward("/versions", version)

    @GET
    @Path("/version/{version}")
    fun version(@PathParam("version") version: String): Uni<Response> = forward("/version", version)
}
