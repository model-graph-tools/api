package org.wildfly.modelgraph.diff

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.diff.JsonDiff
import io.smallrye.mutiny.Uni
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST

@Path("/diff")
@Produces(MediaType.APPLICATION_JSON_PATCH_JSON)
class DiffResource(val registry: Registry) {

    @GET
    fun diff(
        @QueryParam("address") address: String,
        @QueryParam("from") from: String,
        @QueryParam("to") to: String
    ): Uni<Response> = try {
        when {
            from !in registry -> missingVersion(from)
            to !in registry -> missingVersion(to)
            else -> {
                val fromResponse = registry.clients[from]!!
                    .get("/resources/resource")
//                    .putHeader("mgt-anemic", "true")
                    .addQueryParam("address", address)
//                    .timeout(2000)
                    .send()
                val toResponse = registry.clients[to]!!
                    .get("/resources/resource")
//                    .putHeader("mgt-anemic", "true")
                    .addQueryParam("address", address)
//                    .timeout(2000)
                    .send()
                Uni.combine().all().unis(fromResponse, toResponse).asTuple()
                    .map { tuple ->
                        val fromCode = tuple.item1.statusCode()
                        val toCode = tuple.item2.statusCode()
                        val mapper = ObjectMapper()
                        val reader = mapper.reader().withoutAttribute("id")
                        val fromString = tuple.item1.bodyAsString()
                        val fromJson = reader.readTree(fromString)
                        val toString = tuple.item2.bodyAsString()
                        val toJson = reader.readTree(toString)
                        val diff = JsonDiff.asJsonPatch(fromJson, toJson)
                        Response.status(200).entity(diff).build()
                    }
            }
        }
    } catch (throwable: Throwable) {
        Uni.createFrom().item {
            Response.status(BAD_REQUEST.statusCode, throwable.message).build()
        }
    }

    private fun missingVersion(identifier: String) = Uni.createFrom().item {
        Response
            .status(BAD_REQUEST.statusCode, "No model service for $identifier available.")
            .build()
    }
}