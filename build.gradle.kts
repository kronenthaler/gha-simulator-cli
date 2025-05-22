plugins {
    kotlin("jvm") version "2.1.20"
    application
}

group = "com.github.kronenthaler.gha-simulator"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

application {
    mainClass = "com.github.kronenthaler.ghasimulator.SimulatorKt"
}
