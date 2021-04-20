package org.wildfly.modelgraph.model

import io.smallrye.mutiny.Uni
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.HttpRequest
import org.jboss.logging.Logger
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.NOT_FOUND

// Keep this as an interface, not an abstract class!
// Otherwise CDI will grumble about constructor injection in implementations.
interface ModelResource {

    val endpoint: String
    val registry: Registry

    fun forward(
        path: String,
        identifier: String,
        prepareRequest: HttpRequest<Buffer>.() -> HttpRequest<Buffer> = { this }
    ): Uni<Response> = try {
        val client = registry.clients[identifier]
        when {
            client != null -> {
                if (log.isDebugEnabled) {
                    log.debug("Call $endpoint$path/$identifier")
                }
                client.get("$endpoint$path").apply {
                    prepareRequest(this)
                }.send().invoke { response ->
                    if (log.isDebugEnabled) {
                        log.debug("$endpoint$path/$identifier - ${response.statusCode()}")
                    }
                }.map { response ->
                    Response.status(response.statusCode()).entity(response.bodyAsString()).build()
                }
            }
            else -> Uni.createFrom().item(
                Response.status(
                    NOT_FOUND.statusCode,
                    "No model service for $identifier available."
                ).build()
            )
        }
    } catch (throwable: Throwable) {
        Uni.createFrom().item {
            Response.status(BAD_REQUEST.statusCode, throwable.message).build()
        }
    }

    companion object {
        private val log = Logger.getLogger(ModelResource::class.java)
    }
}