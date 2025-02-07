pluginManagement {
    plugins {
        id("org.jetbrains.compose-hot-reload") version "1.0.0-dev-39
    }

    repositories {
        maven(file("../..//build/repo"))
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.1.20-Beta2"
        kotlin("plugin.compose") version "2.1.20-Beta2"
        id("org.jetbrains.compose") version "1.7.3"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        maven(file("../..//build/repo"))
        mavenCentral()
        google()
    }
}
