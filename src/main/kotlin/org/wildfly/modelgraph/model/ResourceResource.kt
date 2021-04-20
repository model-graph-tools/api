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
        @QueryParam("since") since: String
    ): Uni<Response> = forward("/deprecated", identifier) {
        addQueryParam("since", since)
    }

    @GET
    @Path("/resource/{identifier}")
    fun resource(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String,
        @QueryParam("skip") @DefaultValue("") skip: String = ""
    ): Uni<Response> = forward("/resource", identifier) {
        addQueryParam("address", address).apply {
            if (skip.isNotEmpty()) {
                addQueryParam("skip", skip)
            }
        }
    }

    @GET
    @Path("/subtree/{identifier}")
    fun subtree(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String
    ): Uni<Response> = forward("/subtree", identifier) {
        addQueryParam("address", address)
    }

    @GET
    @Path("/children/{identifier}")
    fun children(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String
    ): Uni<Response> = forward("/children", identifier) {
        addQueryParam("address", address)
    }
}
