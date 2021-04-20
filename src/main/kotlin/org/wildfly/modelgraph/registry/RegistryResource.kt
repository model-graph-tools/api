package org.wildfly.modelgraph.registry

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
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
    fun register(registration: Registration): Response = try {
        registry.register(registration)
        Response.status(CREATED).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    @DELETE
    @Path("/{identifier}")
    fun unregister(@PathParam("identifier") identifier: String): Response = try {
        registry.unregister(identifier)
        Response.status(NO_CONTENT).build()
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    @GET
    fun list(): List<Registration> = registry.registrations.values.toList()
}
