// SPDX-License-Identifier: AGPL-3.0-or-later
//
// The server is AGPL-3.0. Nothing here may be linked into client binaries.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

group = "com.gatoryap"
version = "0.1.0"

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

application {
    mainClass.set("com.gatoryap.server.ApplicationKt")
}

dependencies {
    implementation(projects.core)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.datetime)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgres)
    implementation(libs.hikari)
    implementation(libs.postgres.driver)

    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test)
}
