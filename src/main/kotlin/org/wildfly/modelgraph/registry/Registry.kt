package org.wildfly.modelgraph.registry

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.redis.client.reactive.ReactiveRedisClient
import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import io.smallrye.mutiny.Multi
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import org.jboss.logging.Logger
import java.net.URI
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@RegisterForReflection
data class Registration(
    @field:JsonProperty("identifier")
    val identifier: String = "",
    @field:JsonProperty("productName")
    val productName: String = "",
    @field:JsonProperty("productVersion")
    val productVersion: String = "",
    @field:JsonProperty("managementVersion")
    val managementVersion: String = "",
    @field:JsonProperty("modelServiceUri")
    val modelServiceUri: String = "",
    @field:JsonProperty("neo4jBrowserUri")
    val neo4jBrowserUri: String = "",
    @field:JsonProperty("neo4jBoltUri")
    val neo4jBoltUri: String = ""
) {

    internal fun serialize(): String =
        "$productName|$productVersion|$managementVersion|$modelServiceUri|$neo4jBrowserUri|$neo4jBoltUri"

    override fun toString(): String = "$identifier, model service $modelServiceUri, neo4j browser $neo4jBrowserUri"

    companion object {
        internal fun deserialize(identifier: String, data: String): Registration {
            val list = data.split("|")
            require(list.size == 6) { "Unable to parse registration: Malformed data!" }
            return Registration(
                identifier = identifier,
                productName = list[0],
                productVersion = list[1],
                managementVersion = list[2],
                modelServiceUri = list[3],
                neo4jBrowserUri = list[4],
                neo4jBoltUri = list[5]
            )
        }
    }
}

@ApplicationScoped
class Registry(private val vertx: Vertx, private val redis: ReactiveRedisClient) {

    private val _registrations: MutableMap<String, Registration> = mutableMapOf()
    val registrations: Map<String, Registration>
        get() = _registrations

    private val _clients: MutableMap<String, WebClient> = mutableMapOf()
    val clients: Map<String, WebClient>
        get() = _clients

    fun onStart(@Observes event: StartupEvent) {
        log.debug("Execute KEYS $IDENTIFIER_KEY:*")
        redis
            .keys("$IDENTIFIER_KEY:*")
            .invoke { response -> log.debug("Keys: ${response.toList().joinToString()}") }
            .onItem().transformToMulti { response -> Multi.createFrom().iterable(response) }
            .onItem().transform { response -> response.toString() }
            .onItem().transformToUniAndMerge { identifierKey ->
                log.debug("Execute GET for $identifierKey")
                redis.get(identifierKey).onItem().transform { response ->
                    log.debug("Data: $response")
                    identifierKey.substringAfter("$IDENTIFIER_KEY:") to response.toString()
                }
            }
            .subscribe().with { (identifier, data) ->
                try {
                    log.debug("Use $identifier and $data for registration")
                    register(Registration.deserialize(identifier, data))
                } catch (e: Exception) {
                    log.error("Unable to register $identifier and $data: ${e.message}")
                }
            }
    }

    fun register(registration: Registration) {
        log.debug("register($registration)")
        try {
            _registrations[registration.identifier] = registration
            val uri = URI(registration.modelServiceUri)
            val options = WebClientOptions().apply {
                defaultHost = uri.host
                defaultPort = uri.port
                userAgent = "mgt-api"
            }
            log.debug("Create web client for $registration")
            _clients[registration.identifier] = WebClient.create(vertx, options)
            log.debug("Web client successfully created")
            log.debug("Execute SET ${identifyKey(registration.identifier)} ${registration.serialize()}")
            redis.set(listOf(identifyKey(registration.identifier), registration.serialize())).subscribe().with {
                log.info("Registered $registration")
            }
        } catch (e: Exception) {
            log.error("Unable to register $registration: ${e.message}")
        }
    }

    fun unregister(identifier: String) {
        log.debug("unregister($identifier)")
        try {
            _registrations.remove(identifier)
            _clients.remove(identifier)?.close()
            log.debug("Execute DEL ${identifyKey(identifier)}")
            redis.del(listOf(identifyKey(identifier))).subscribe().with {
                log.info("Unregistered $identifier")
            }
        } catch (e: Exception) {
            log.error("Unable to unregister $identifier: ${e.message}")
        }
    }

    operator fun contains(identifier: String) = identifier in _registrations && identifier in _clients

    private fun identifyKey(identifier: String) = "$IDENTIFIER_KEY:$identifier"

    companion object {
        private const val MGT_PREFIX = "mgt"
        private const val IDENTIFIER_KEY = "$MGT_PREFIX:identifier"
        private val log = Logger.getLogger(Registry::class.java)
    }
}
