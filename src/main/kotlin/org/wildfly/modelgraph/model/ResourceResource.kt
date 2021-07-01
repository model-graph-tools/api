package org.wildfly.modelgraph.model

import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
class ResourceResource(
    override val registry: Registry,
    override val config: Config
) : ModelResource {

    override val endpoint: String = "/resources"

    @GET
    @Path("/query/{identifier}")
    suspend fun query(
        @PathParam("identifier") identifier: String,
        @QueryParam("name") name: String
    ): Response = forward("/query", identifier) {
        addQueryParam("name", name)
    }

    @GET
    @Path("/deprecated/{identifier}")
    suspend fun deprecated(
        @PathParam("identifier") identifier: String,
        @QueryParam("since") @DefaultValue("") since: String = ""
    ): Response = forward("/deprecated", identifier) {
        addQueryParam("since", since)
    }

    @GET
    @Path("/resource/{identifier}")
    suspend fun resource(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String,
        @QueryParam("skip") @DefaultValue("") skip: String = "",
        @Context headers: HttpHeaders
    ): Response = forward("/resource", identifier) {
        if (headers.getRequestHeader("mgt-diff").isNotEmpty()) {
            putHeader("mgt-diff", "true")
        }
        addQueryParam("address", address).apply {
            if (skip.isNotEmpty()) {
                addQueryParam("skip", skip)
            }
        }
    }

    @GET
    @Path("/subtree/{identifier}")
    suspend fun subtree(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String
    ): Response = forward("/subtree", identifier) {
        addQueryParam("address", address)
    }

    @GET
    @Path("/children/{identifier}")
    suspend fun children(
        @PathParam("identifier") identifier: String,
        @QueryParam("address") address: String
    ): Response = forward("/children", identifier) {
        addQueryParam("address", address)
    }
}
