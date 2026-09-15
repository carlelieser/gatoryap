// SPDX-License-Identifier: Apache-2.0
//
// Android entry point. Separating this from the shared modules is mandatory
// under AGP 9 — the KMP plugin and com.android.application cannot be applied to
// the same module. AGP 9 has built-in Kotlin support, so the Kotlin Android
// plugin is not applied here.
// https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.gatoryap.android"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gatoryap.android"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidCompileSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    dependencies {
        implementation(projects.app.composeApp)
        implementation(libs.androidx.activity.compose)
    }
}
