package org.wildfly.modelgraph.diff

import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST

@Path("/diff")
@Produces(MediaType.APPLICATION_JSON)
class DiffResource(val registry: Registry) {

    @GET
    fun diff(
        @QueryParam("address") address: String,
        @QueryParam("from") from: String,
        @QueryParam("to") to: String
    ): Response = try {
        when {
            from !in registry -> missingVersion(from)
            to !in registry -> missingVersion(to)
            else -> {
                Response.status(Response.Status.NO_CONTENT).build()
            }
        }
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    private fun missingVersion(identifier: String) =
        Response
            .status(BAD_REQUEST.statusCode, "No model service for $identifier available.")
            .build()
}