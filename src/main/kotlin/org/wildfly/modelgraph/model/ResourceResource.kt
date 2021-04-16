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

@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
class ResourceResource(override val registry: Registry) : ModelResource {

    override val endpoint: String = "/resources"

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
    @Path("/resource/{version}")
    fun resource(
        @PathParam("version") version: String,
        @QueryParam("address") address: String,
        @QueryParam("skip") @DefaultValue("") skip: String = ""
    ): Uni<Response> = forward("/resource", version) {
        addQueryParam("address", address).apply {
            if (skip.isNotEmpty()) {
                addQueryParam("skip", skip)
            }
        }
    }

    @GET
    @Path("/subtree/{version}")
    fun subtree(
        @PathParam("version") version: String,
        @QueryParam("address") address: String
    ): Uni<Response> = forward("/subtree", version) {
        addQueryParam("address", address)
    }

    @GET
    @Path("/children/{version}")
    fun children(
        @PathParam("version") version: String,
        @QueryParam("address") address: String
    ): Uni<Response> = forward("/children", version) {
        addQueryParam("address", address)
    }
}
