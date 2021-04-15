package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import org.wildfly.modelgraph.registry.Registry
import org.wildfly.modelgraph.registry.Version
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/management-model")
class ManagementModelResource(val registry: Registry) {

    @GET
    @Path("/query/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    fun query(@PathParam("version") versionString: String, @QueryParam("name") name: String): Uni<JsonObject> {
        val version = Version.parse(versionString)
        val client = registry.clients[version]
        return if (client != null) {
            client
                .get("/query")
                .addQueryParam("name", name)
                .send()
                .onItem().transform { response ->
                    response.bodyAsJsonObject()
                }
        } else {
            Uni.createFrom().item(JsonObject("{}"))
        }
    }
}
