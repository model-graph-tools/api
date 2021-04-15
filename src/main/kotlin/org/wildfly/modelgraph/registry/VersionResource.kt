package org.wildfly.modelgraph.registry

import com.fasterxml.jackson.annotation.JsonProperty
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

data class VersionAndBrowser(
    @field:JsonProperty("version")
    val version: Version? = null,
    @field:JsonProperty("browser")
    val browser: String? = null
)

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource(val registry: Registry) {

    @GET
    fun list(): List<VersionAndBrowser> = registry.browsers.map { (version, browser) ->
        VersionAndBrowser(version, browser.toString())
    }
}
