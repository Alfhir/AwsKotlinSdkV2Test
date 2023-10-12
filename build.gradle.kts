plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "poc.alfhir"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.http4k.core)
    implementation(libs.http4k.okhttp)
    implementation(libs.http4k.aws)
    implementation(libs.http4k.undertow)
    implementation(libs.aws.kotlin.sdk)
    // used for delay
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.implementation)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}