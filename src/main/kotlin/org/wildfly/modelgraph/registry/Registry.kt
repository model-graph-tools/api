package org.wildfly.modelgraph.registry

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.redis.client.reactive.ReactiveRedisClient
import io.quarkus.runtime.StartupEvent
import io.smallrye.mutiny.Multi
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import org.jboss.logging.Logger
import java.net.URI
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

data class Registration(
    @field:JsonProperty("version")
    val version: String? = null,
    @field:JsonProperty("service")
    val service: String? = null,
    @field:JsonProperty("browser")
    val browser: String? = null
)

data class Version(
    @field:JsonProperty("major")
    val major: Int = 0,
    @field:JsonProperty("minor")
    val minor: Int = 0,
    @field:JsonProperty("patch")
    val patch: Int = 0
) : Comparable<Version> {

    override fun compareTo(other: Version): Int = when {
        this.major != other.major -> this.major - other.major
        this.minor != other.minor -> this.minor - other.minor
        this.patch != other.patch -> this.patch - other.patch
        else -> 0
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(value: String): Version {
            val parts = value.split('.')
            require(parts.size == 3) { "Malformed version" }
            return Version(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}

@ApplicationScoped
class Registry(private val vertx: Vertx, private val redis: ReactiveRedisClient) {

    private val _services: MutableMap<Version, URI> = mutableMapOf()
    val services: Map<Version, URI>
        get() = _services

    private val _browsers: MutableMap<Version, URI> = mutableMapOf()
    val browsers: Map<Version, URI>
        get() = _browsers

    private val _clients: MutableMap<Version, WebClient> = mutableMapOf()
    val clients: Map<Version, WebClient>
        get() = _clients

    fun onStart(@Observes event: StartupEvent) {
        redis
            .keys("$VERSION_KEY:*")
            .onItem().transformToMulti { response -> Multi.createFrom().iterable(response) }
            .onItem().transform { response -> response.toString() }
            .onItem().transformToUniAndMerge { version ->
                redis.hgetall(version).onItem().transform { response ->
                    version to response.toList().map { it.toString() }
                }
            }
            .subscribe().with { versionWithValues ->
                try {
                    val version = Version.parse(versionWithValues.first.substringAfter("$VERSION_KEY:"))
                    val service = URI(versionWithValues.second[0])
                    val browser = URI(versionWithValues.second[1])
                    register(version, service, browser)
                } catch (e: Exception) {
                    LOGGER.error(
                        "Unable to register model service ${versionWithValues.first} loaded from redis: ${e.message}"
                    )
                }
            }
    }

    fun register(version: Version, service: URI, browser: URI) {
        try {
            _services[version] = service
            _browsers[version] = browser
            val options = WebClientOptions().apply {
                defaultHost = service.host
                defaultPort = service.port
                userAgent = "mgt-api"
            }
            _clients[version] = WebClient.create(vertx, options)
            redis.hset(
                listOf(
                    versionKey(version),
                    service.toString(),
                    browser.toString()
                )
            ).subscribe().with {
                LOGGER.info("Registered model service $version")
            }
        } catch (e: Exception) {
            LOGGER.error("Unable to register model service $version: ${e.message}")
        }
    }

    fun unregister(version: Version) {
        try {
            _services.remove(version)
            _clients.remove(version)?.close()
            redis.del(listOf(versionKey(version))).subscribe().with {
                LOGGER.info("Unregistered model service $version")
            }
        } catch (e: Exception) {
            LOGGER.error("Unable to unregister model service $version: ${e.message}")
        }
    }

    operator fun contains(version: Version) = version in _services

    private fun versionKey(version: Version) = "$VERSION_KEY:$version"

    companion object {
        private const val VERSION_KEY = "mgt:version"
        private val LOGGER = Logger.getLogger(Registry::class.java)
    }
}
