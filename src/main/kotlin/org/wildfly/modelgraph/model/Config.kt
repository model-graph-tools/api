package org.wildfly.modelgraph.model

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "mgt.api")
interface Config {

    fun timeout(): Long
}