package org.jetbrains.compose.reload.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.Locale

@InternalHotReloadGradleApi
val String.capitalized
    get() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@InternalHotReloadGradleApi
val Project.kotlinMultiplatformOrNull: KotlinMultiplatformExtension?
    get() = extensions.getByName("kotlin") as? KotlinMultiplatformExtension

@InternalHotReloadGradleApi
val Project.kotlinJvmOrNull: KotlinJvmProjectExtension?
    get() = extensions.getByName("kotlin") as? KotlinJvmProjectExtension

@InternalHotReloadGradleApi
fun Project.files(lazy: () -> Any) = files({ lazy() })

@InternalHotReloadGradleApi
fun Project.withComposePlugin(block: () -> Unit) {
    pluginManager.withPlugin("org.jetbrains.compose") {
        block()
    }
}

@InternalHotReloadGradleApi
fun Project.withComposeCompilerPlugin(block: () -> Unit) {
    pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
        block()
    }
}

@InternalHotReloadGradleApi
fun Project.withKotlinPlugin(block: () -> Unit) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        block()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        block()
    }
}
