pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version "1.13.4.Final"
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.10.0"
}

rootProject.name = "model-graph-api"
