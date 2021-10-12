package org.wildfly.modelgraph.model

import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.HttpRequest
import org.jboss.logging.Logger
import org.wildfly.modelgraph.registry.Registry
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.NOT_FOUND

// Keep this as an interface, not an abstract class!
// Otherwise, CDI will grumble about constructor injection in implementations.
interface ModelResource {

    val endpoint: String
    val registry: Registry
    val config: Config

    suspend fun forward(
        path: String,
        identifier: String,
        prepareRequest: HttpRequest<Buffer>.() -> HttpRequest<Buffer> = { this }
    ): Response = try {
        val client = registry.clients[identifier]
        if (client != null) {
            if (log.isDebugEnabled) {
                log.debug("Call $endpoint$path for $identifier")
            }
            client.get("$endpoint$path").apply {
                prepareRequest(this)
            }.timeout(config.timeout()).send().map { response ->
                if (log.isDebugEnabled) {
                    log.debug("$endpoint$path for $identifier returned ${response.statusCode()}")
                }
                Response.status(response.statusCode()).entity(response.bodyAsString()).build()
            }.awaitSuspending()
        } else {
            Response.status(NOT_FOUND.statusCode, "No model service for $identifier available.").build()
        }
    } catch (throwable: Throwable) {
        Response.status(BAD_REQUEST.statusCode, throwable.message).build()
    }

    companion object {
        private val log = Logger.getLogger(ModelResource::class.java)
    }
}