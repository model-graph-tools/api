import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    id("io.quarkus") version "1.13.1.Final"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.31"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:1.13.1.Final"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-resteasy-mutiny")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.smallrye.reactive:mutiny-kotlin:0.15.0")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client:2.3.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
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
