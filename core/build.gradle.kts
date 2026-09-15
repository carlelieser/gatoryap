// SPDX-License-Identifier: Apache-2.0
//
// Shared between the server and the clients: models, validation, API contract.
// Linked into App Store binaries, so this module MUST stay Apache-2.0 and MUST
// NOT depend on AGPL code. Server-only logic belongs in :server.

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    androidLibrary {
        namespace = "com.gatoryap.core"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
