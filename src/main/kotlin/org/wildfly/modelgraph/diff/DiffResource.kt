package org.wildfly.modelgraph.diff

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.diff.JsonDiff
import io.smallrye.mutiny.Uni
import org.wildfly.modelgraph.registry.Registry
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.Family.SUCCESSFUL
import javax.ws.rs.core.Response.Status.Family.familyOf

@Path("/diff")
@Produces(MediaType.APPLICATION_JSON_PATCH_JSON)
class DiffResource(val registry: Registry) {

    @Inject
    lateinit var objectMapper: ObjectMapper

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
                Uni.combine().all().unis(
                    registry.clients[from]!!
                        .get("/resources/resource")
                        .putHeader("mgt-diff", "true")
                        .addQueryParam("address", address)
                        .send(),
                    registry.clients[to]!!
                        .get("/resources/resource")
                        .putHeader("mgt-diff", "true")
                        .addQueryParam("address", address)
                        .send()
                ).asTuple().map { tuple ->
                    val fromResponse = tuple.item1
                    val toResponse = tuple.item2
                    if (familyOf(fromResponse.statusCode()) == SUCCESSFUL &&
                        familyOf(toResponse.statusCode()) == SUCCESSFUL
                    ) {
                        val diff = JsonDiff.asJsonPatch(
                            objectMapper.reader().readTree(fromResponse.bodyAsString()),
                            objectMapper.reader().readTree(toResponse.bodyAsString())
                        )
                        Response.status(200).entity(diff).build()
                    } else {
                        Response.status(
                            BAD_REQUEST.statusCode,
                            "from: ${fromResponse.statusCode()}, to: ${fromResponse.statusCode()}"
                        ).build()
                    }
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