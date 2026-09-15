pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Lets Gradle download the pinned JDK (see libs.versions.toml) on any machine,
// so a clean clone builds regardless of which JDK happens to be installed.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "gatoryap"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Shared between server and clients: models, validation.
include(":core")

// Server-specific code.
include(":server")

// Client entry points and shared client code.
include(":app:composeApp")
include(":app:androidApp")
