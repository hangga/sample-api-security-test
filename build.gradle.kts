plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.github.hangga"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("io.ktor:ktor-client-core:2.0.0")
    testImplementation("io.ktor:ktor-client-cio:2.0.0")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")

    testImplementation("org.wiremock:wiremock:3.10.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}