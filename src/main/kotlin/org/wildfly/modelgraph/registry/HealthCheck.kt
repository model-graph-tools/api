package org.wildfly.modelgraph.registry

import io.quarkus.scheduler.Scheduled
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped

@Liveness
@ApplicationScoped
@Suppress("unused")
class HealthCheck(private val registry: Registry) : org.eclipse.microprofile.health.HealthCheck {

    private val modelServiceHealth: MutableMap<String, Int> = mutableMapOf()

    @Scheduled(every = "10s")
    fun checkModels() {
        registry.clients.forEach { (identifier, client) ->
            log.debug("Check health for $identifier")
            client.get("/q/health")
                .send()
                .onFailure().invoke { throwable ->
                    unregisterOnFailure(identifier, throwable.message ?: "n/a")
                }
                .subscribe().with { response ->
                    log.debug("Health check for $identifier returned ${response.statusCode()}")
                    modelServiceHealth[identifier] = response.statusCode()
                    if (response.statusCode() != 200) {
                        unregisterOnFailure(identifier, "status code ${response.statusCode()}")
                    }
                }
        }
    }

    override fun call(): HealthCheckResponse {
        val builder = HealthCheckResponse.named("Model services").up()
        modelServiceHealth.forEach { (identifier, status) ->
            builder.withData(identifier, status.toLong())
        }
        return builder.build()
    }

    private fun unregisterOnFailure(identifier: String, reason: String) {
        log.error("Health check for $identifier failed: $reason")
        modelServiceHealth.remove(identifier)
        registry.unregister(identifier)
    }

    companion object {
        private val log = Logger.getLogger(HealthCheck::class.java)
    }
}
