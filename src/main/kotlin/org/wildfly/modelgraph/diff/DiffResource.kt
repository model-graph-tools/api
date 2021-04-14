package org.wildfly.modelgraph.diff

import org.wildfly.modelgraph.registry.Registry
import org.wildfly.modelgraph.registry.Version
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
        val versionFrom = Version.parse(from)
        val versionTo = Version.parse(to)
        when {
            versionFrom !in registry -> missingVersion(versionFrom)
            versionTo !in registry -> missingVersion(versionTo)
            else -> {
                Response.status(Response.Status.NO_CONTENT).build()
            }
        }
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    private fun missingVersion(version: Version) =
        Response
            .status(BAD_REQUEST.statusCode, "No management model service for $version available.")
            .build()
}