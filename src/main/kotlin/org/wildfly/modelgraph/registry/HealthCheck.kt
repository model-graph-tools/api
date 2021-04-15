package org.wildfly.modelgraph.registry

import io.quarkus.scheduler.Scheduled
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class HealthCheck(private val registry: Registry) {

    @Scheduled(every = "10s")
    fun checkModels() {
        registry.clients.forEach { (version, client) ->
            // TODO Execute health check
            client.get("/q/health")
                .send()
                .onFailure().invoke { throwable ->
                    unregister(version, "status code ${throwable.message}")
                }
                .subscribe().with { response ->
                    if (response.statusCode() == 200) {
                        LOGGER.info("Health check for model service $version successful")
                    } else {
                        unregister(version, "status code ${response.statusCode()}")
                    }
                }
        }
    }

    fun unregister(version: Version, reason: String) {
        LOGGER.error("Health check for model service $version failed: $reason")
        registry.unregister(version)
    }

    companion object {
        private val LOGGER = Logger.getLogger(HealthCheck::class.java)
    }
}
