pluginManagement {
    val quarkusPluginVersion: String by settings
    val kotlinVersion: String by settings
    val nodePlugin: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version quarkusPluginVersion
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("com.github.node-gradle.node") version nodePlugin
    }
}
rootProject.name="quarkus-astro-starter"

val apps = File("apps")
val libs = File("libs")

loadSubProjects(listOf(apps, libs))

fun loadSubProjects(modules: List<File>) {
    modules.forEach { module ->
        if (module.exists()) {
            if (module.isDirectory) {
                module.listFiles()?.forEach { submodule ->
                    if (submodule.isDirectory) {
                        println("Loading submodule \uD83D\uDCE6: ${submodule.name}")
                        include(":${submodule.name}")
                        project(":${submodule.name}").projectDir = File("${module.name}/${submodule.name}")
                    } else {
                        println("${submodule.name} is not a directory \uD83D\uDDFF - skipping")
                    }
                }
            } else {
                println("${module.name} is not a directory \uD83D\uDE12 - ${module.name}")
            }
        } else {
            println("${module.name} directory does not exist \uD83D\uDEAB - ${module.name}")
        }
    }
}

