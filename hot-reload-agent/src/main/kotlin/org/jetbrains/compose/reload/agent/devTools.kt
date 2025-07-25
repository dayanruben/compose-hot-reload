/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload.agent

import org.jetbrains.compose.reload.core.Environment
import org.jetbrains.compose.reload.core.HotReloadEnvironment
import org.jetbrains.compose.reload.core.HotReloadEnvironment.devToolsDetached
import org.jetbrains.compose.reload.core.HotReloadProperty.DevToolsClasspath
import org.jetbrains.compose.reload.core.HotReloadProperty.Environment.DevTools
import org.jetbrains.compose.reload.core.Os
import org.jetbrains.compose.reload.core.createLogger
import org.jetbrains.compose.reload.core.error
import org.jetbrains.compose.reload.core.getBlocking
import org.jetbrains.compose.reload.core.getOrThrow
import org.jetbrains.compose.reload.core.info
import org.jetbrains.compose.reload.core.issueNewDebugSessionJvmArguments
import org.jetbrains.compose.reload.core.subprocessDefaultArguments
import org.jetbrains.compose.reload.core.withHotReloadEnvironmentVariables
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private val logger = createLogger()

internal fun launchDevtoolsApplication() {
    if (!HotReloadEnvironment.devToolsEnabled) return
    val classpath = HotReloadEnvironment.devToolsClasspath ?: error("Missing '${DevToolsClasspath}'")
    logger.info("Starting 'DevTools'")

    val process = ProcessBuilder(
        resolveDevtoolsJavaBinary(),
        *platformSpecificJvmArguments(),
        "-cp", classpath.joinToString(File.pathSeparator),
        *subprocessDefaultArguments(DevTools, orchestration.port.getBlocking().getOrThrow()).toTypedArray(),
        *issueNewDebugSessionJvmArguments("DevTools"),
        "org.jetbrains.compose.devtools.Main",
    )
        .withHotReloadEnvironmentVariables(DevTools)
        .start()

    thread(name = "DevTools: Stderr", isDaemon = true) {
        val stderrLogger = createLogger("DevTools: Stderr", environment = Environment.devTools)
        process.errorStream.bufferedReader().forEachLine { line ->
            stderrLogger.error(line)
        }
    }
}

private fun resolveDevtoolsJavaBinary(): String? {
    fun Path.resolveJavaHome(): Path = resolve(
        if (Os.currentOrNull() == Os.Windows) "bin/java.exe" else "bin/java"
    )

    System.getProperty("java.home")?.let { javaHome ->
        return Path(javaHome).resolveJavaHome().absolutePathString()
    }

    System.getenv("JAVA_HOME")?.let { javaHome ->
        return Path(javaHome).resolveJavaHome().absolutePathString()
    }

    return null
}

private fun platformSpecificJvmArguments(): Array<String> = when (Os.current()) {
    // Allow reflective access to X11 classes for Linux
    // Required to properly set the app name in the taskbar
    Os.Linux -> arrayOf("--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED")
    // Disable dock icon when not running in detached mode for MacOS
    Os.MacOs -> arrayOf(
        "-Dapple.awt.UIElement=${!devToolsDetached}",
        "-Dapple.awt.application.name=Compose Hot Reload Dev Tools",
    )
    // We need no enable OpenGL rendering for skiko, because
    // DirectX rendering has issues with managing transparency/opacity
    // and click-through
    Os.Windows -> arrayOf("-Dskiko.renderApi=OPENGL")
}
