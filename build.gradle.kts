import groovy.json.JsonOutput
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version libs.versions.kotlinVer
    kotlin("plugin.serialization") version libs.versions.kotlinVer

    alias(libs.plugins.git)
    alias(libs.plugins.shadow)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

allprojects {
    group = "moe.caa"
    version = "0.8.0"

    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "net.kyori.indra.git")
    apply(plugin = "com.github.johnrengelman.shadow")

    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17

    tasks.processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        filter(
            ReplaceTokens::class, mapOf(
                "tokens" to buildData,
                "beginToken" to "@",
                "endToken" to "@"
            )
        )
    }

    tasks.shadowJar {
        configurations = listOf()
        archiveBaseName = ""
        archiveVersion = outputArchiveVersion
        archiveClassifier = ""

        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
                    "Build-Revision" to indraGit.commit()?.name(),
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${
                        System.getProperty(
                            "java.vm.version"
                        )
                    })",
                    "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${
                        System.getProperty(
                            "os.version"
                        )
                    }"
                )
            )
        }
    }
}


val contributors = project.rootProject.file("contributors").readLines().map { it.trim() }.toSet()
val contributorsJson: String = JsonOutput.toJson(contributors)
val outputArchiveVersion: String = System.getProperty("build_type", "auto")
    .equals("final", true).ifTrue {
        version.toString()
    } ?: indraGit.commit()?.name().let {
    "Build_${it?.substring(0, 8) ?: "unknown"}"
}

val buildData by extra(
    fun(): Map<String, String> = mapOf(
        "version" to outputArchiveVersion,
        "build_by" to System.getProperty("user.name"),
        "contributors_json" to contributorsJson,
        "contributors" to contributors.joinToString(),
        "build_type" to System.getProperty("build_type", "auto"),
        "build_revision" to (indraGit.commit()?.name ?: "unknown"),
        "build_timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
    )
)