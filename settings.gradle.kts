@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        maven(file("build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev") {
            mavenContent {
                includeModuleByRegex("org.jetbrains.compose", "hot-reload.*")
                includeModuleByRegex("org.jetbrains.compose-hot-reload.*", ".*")
            }
        }

        gradlePluginPortal {
            content {
                includeGroupByRegex("org.gradle.*")
                includeGroupByRegex("com.gradle.*")
            }
        }
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.compose-hot-reload-test") version providers.gradleProperty("bootstrap.version").get()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

/*
Configure Repositories / Dependencies
*/
dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("dependencies.toml"))
        }
    }

    repositories {
        repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

        maven(file("build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev") {
            mavenContent {
                includeModuleByRegex("org.jetbrains.compose", "hot-reload.*")
            }
        }

        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*androidx.*")
                includeGroupByRegex(".*google.*")
            }
        }

        mavenCentral()
    }
}

include(":hot-reload-core")
include(":hot-reload-analysis")
include(":hot-reload-agent")
include(":hot-reload-orchestration")
include(":hot-reload-gradle-plugin")
include(":hot-reload-gradle-utils")
include(":hot-reload-runtime-api")
include(":hot-reload-runtime-jvm")
include(":hot-reload-devtools")
include(":hot-reload-test")
include(":hot-reload-test:gradle-plugin")
include(":hot-reload-under-test")
include(":tests:functionalTests")
include(":tests:unitTests")


gradle.beforeProject {
    group = "org.jetbrains.compose"
    version = project.providers.gradleProperty("version").get()

    plugins.apply("test-conventions")
    plugins.apply("main-conventions")
    plugins.apply("kotlin-conventions")
}
