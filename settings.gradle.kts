pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version "1.13.1.Final"
    }
}

rootProject.name = "model-graph-api"
