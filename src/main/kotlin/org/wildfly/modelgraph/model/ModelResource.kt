package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.HttpRequest
import org.jboss.logging.Logger
import org.wildfly.modelgraph.registry.Registry
import org.wildfly.modelgraph.registry.Version
import javax.ws.rs.core.Response

// Keep this as an interface, not an abstract class!
// Otherwise CDI will grumble about constructor injection in implementations.
interface ModelResource {

    val endpoint: String
    val registry: Registry

    fun forward(
        path: String,
        version: String,
        prepareRequest: HttpRequest<Buffer>.() -> HttpRequest<Buffer> = { this }
    ): Uni<Response> = try {
        val client = registry.clients[Version.parse(version)]
        when {
            client != null -> {
                if (log.isDebugEnabled) {
                    log.debug("Call $endpoint$path/$version")
                }
                client.get("$endpoint$path").apply {
                    prepareRequest(this)
                }.send().invoke { response ->
                    if (log.isDebugEnabled) {
                        log.debug("$endpoint/$path/$version - ${response.statusCode()}")
                    }
                }.map { response ->
                    Response.status(response.statusCode()).entity(response.bodyAsString()).build()
                }
            }
            else -> Uni.createFrom().item(
                Response.status(
                    Response.Status.NOT_FOUND.statusCode,
                    "Model service for version $version not found"
                ).build()
            )
        }
    } catch (throwable: Throwable) {
        Uni.createFrom().item {
            Response.status(Response.Status.BAD_REQUEST.statusCode, throwable.message).build()
        }
    }

    companion object {
        private val log = Logger.getLogger(ModelResource::class.java)
    }
}