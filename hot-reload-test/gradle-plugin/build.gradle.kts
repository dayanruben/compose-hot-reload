plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    `publishing-conventions`
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        this.artifactId = "hot-reload-test-gradle-plugin"
    }
}

gradlePlugin {
    plugins.create("hot-reload-test") {
        id = "org.jetbrains.compose-hot-reload-test"
        implementationClass = "org.jetbrains.compose.reload.test.HotReloadTestPlugin"
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin"))
    implementation(project(":hot-reload-gradle-utils"))
    implementation(project(":hot-reload-core"))
    implementation(project(":hot-reload-orchestration"))
    implementation(deps.asm)
    implementation(deps.asm.tree)
    implementation(deps.logback)
}
