pluginManagement {
    plugins {
        id("org.jetbrains.compose-hot-reload") version "1.0.0-dev.29.2"
    }

    repositories {
        maven(file("../..//build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.1.0-firework.29"
        kotlin("plugin.compose") version "2.1.0-firework.29"
        id("org.jetbrains.compose") version "1.7.1"
    }
}

dependencyResolutionManagement {
    repositories {
        maven(file("../..//build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
        mavenCentral()
        google()
    }
}

include(":app")
include(":widgets")