package org.wildfly.modelgraph.registry

import org.jboss.logging.Logger
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

data class ModelService(val version: String = "", val url: String = "") {
    override fun toString() = "$version @ $url"
}

@Path("/registry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RegistryResource(val registry: Registry) {

    private val logger = Logger.getLogger(RegistryResource::class.java)

    @POST
    fun registerModelService(service: ModelService): Response = try {
        val version = Version.parse(service.version)
        val uri = URI(service.url)
        registry.modelServices[version] = uri
        logger.info("Registered model service $service")
        Response.status(CREATED).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    @DELETE
    @Path("/{version}")
    fun unregisterModelService(@PathParam("version") versionString: String): Response = try {
        val version = Version.parse(versionString)
        registry.modelServices.remove(version)
        logger.info("Unregistered model service $version")
        Response.status(NO_CONTENT).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }
}
