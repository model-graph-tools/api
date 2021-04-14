package org.wildfly.modelgraph.registry

import java.net.URI
import javax.enterprise.context.ApplicationScoped

data class Version(val major: Int = 0, val minor: Int = 0, val patch: Int = 0) : Comparable<Version> {

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
class Registry {

    val modelServices: MutableMap<Version, URI> = mutableMapOf()

    operator fun contains(version: Version) = version in modelServices
}
