package org.wildfly.modelgraph.registry

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED
import javax.ws.rs.core.Response.Status.NO_CONTENT

@Path("/registry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RegistryResource(val registry: Registry) {

    @POST
    fun register(service: Registration): Response = try {
        val version = Version.parse(service.version ?: "")
        val serviceUri = URI(service.service ?: "")
        val browserUri = URI(service.browser ?: "")
        registry.register(version, serviceUri, browserUri)
        Response.status(CREATED).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    @DELETE
    @Path("/{version}")
    fun unregister(@PathParam("version") versionString: String): Response = try {
        val version = Version.parse(versionString)
        registry.unregister(version)
        Response.status(NO_CONTENT).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }
}
