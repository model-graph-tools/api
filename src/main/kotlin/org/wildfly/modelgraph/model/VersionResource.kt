package org.wildfly.modelgraph.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.ext.web.client.HttpResponse
import org.jboss.logging.Logger
import org.wildfly.modelgraph.registry.Registry
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.OK

data class Version(
    @field:JsonProperty("id")
    val id: String = "",
    @field:JsonProperty("modelType")
    val modelType: String = "",
    @field:JsonProperty("major")
    val major: Int = 0,
    @field:JsonProperty("minor")
    val minor: Int = 0,
    @field:JsonProperty("patch")
    val patch: Int = 0,
    @field:JsonProperty("ordinal")
    val ordinal: Int = 0
)

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource(override val registry: Registry) : ModelResource {

    @Inject
    lateinit var objectMapper: ObjectMapper

    override val endpoint: String = "/versions"

    @GET
    fun versions(): Uni<Response> = Uni.combine().all().unis<Uni<HttpResponse<*>>>(
        registry.clients.values.map { it.get("/versions").send() }
    ).combinedWith { responses ->
        responses.filterIsInstance<HttpResponse<*>>().flatMap { response ->
            response.bodyAsJsonArray().map { objectMapper.readValue(it.toString(), Version::class.java) }
        }.distinctBy { it.ordinal }.sortedBy { it.ordinal }.reversed()
    }.map { versions -> Response.status(OK).entity(versions).build() }

    @GET
    @Path("/{identifier}")
    fun versions(@PathParam("identifier") identifier: String): Uni<Response> = forward("/", identifier)

    companion object {
        private val log = Logger.getLogger(VersionResource::class.java)
    }
}
