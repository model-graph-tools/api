pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version "2.0.0.Final"
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.10.1"
}

rootProject.name = "model-graph-api"
