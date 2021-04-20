package org.wildfly.modelgraph.registry

import io.quarkus.scheduler.Scheduled
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Suppress("unused")
class HealthCheck(private val registry: Registry) {

    @Scheduled(every = "10s")
    fun checkModels() {
        registry.clients.forEach { (version, client) ->
            log.debug("Check health for $version")
            client.get("/q/health")
                .send()
                .onFailure().invoke { throwable ->
                    unregisterOnFailure(version, throwable.message ?: "n/a")
                }
                .subscribe().with { response ->
                    log.debug("Health check for $version returned ${response.statusCode()}")
                    if (response.statusCode() != 200) {
                        unregisterOnFailure(version, "status code ${response.statusCode()}")
                    }
                }
        }
    }

    private fun unregisterOnFailure(identifier: String, reason: String) {
        log.error("Health check for $identifier failed: $reason")
        registry.unregister(identifier)
    }

    companion object {
        private val log = Logger.getLogger(HealthCheck::class.java)
    }
}
