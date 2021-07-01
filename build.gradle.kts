import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.quarkus")
    id("org.jetbrains.kotlin.plugin.allopen")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:_"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.smallrye.reactive:mutiny-kotlin:_")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client:_")
    implementation("com.github.java-json-tools:json-patch:_");
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:_")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:kotlin-extensions")
}

group = "org.wildfly.modelgraph"
version = "0.0.1"

allOpen {
    annotations(
        "javax.enterprise.context.ApplicationScoped",
        "javax.ws.rs.Path",
        "io.quarkus.test.junit.QuarkusTest"
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}
