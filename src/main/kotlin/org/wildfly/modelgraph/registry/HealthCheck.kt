package org.wildfly.modelgraph.registry

import io.quarkus.scheduler.Scheduled
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class HealthCheck(val registry: Registry) {

    @Scheduled(every = "10s")
    fun checkModels() {
        registry.modelServices.forEach { (version, uri) ->
            LOGGER.info("Check health of model service $version @ $uri")
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger(HealthCheck::class.java)
    }
}
