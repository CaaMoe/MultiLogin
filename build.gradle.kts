import groovy.json.JsonOutput
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.text.SimpleDateFormat
import java.util.*
import java.util.Optional

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.git)
}


java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

allprojects {
    group = "moe.caa"
    version = project.properties["plugin_version"] as String

    repositories {
        mavenCentral()
        google()
    }
}


kotlin {
    jvmToolchain(21)
}


subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "net.kyori.indra.git")
    apply(plugin = "com.github.johnrengelman.shadow")

    java.sourceCompatibility = rootProject.java.sourceCompatibility
    java.targetCompatibility = rootProject.java.targetCompatibility

    tasks.shadowJar {
        configurations = listOf()

        archiveAppendix = "${rootProject.name}-${project.name.substring(rootProject.name.length + 1).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
        archiveBaseName = ""
        archiveVersion = buildArchiveVersion
        archiveClassifier = ""

        manifest {
            attributes(
                mapOf(
                    "Build-By" to System.getProperty("user.name"),
                    "Build-Timestamp" to System.currentTimeMillis(),
                    "Build-Datetime" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
                    "Build-Revision" to (indraGit.commit()?.name?: "unknown"),
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

    tasks.named("build"){
        finalizedBy(tasks.named("shadowJar"))
    }
}


val contributors = project.rootProject.file("contributors").readLines().map { it.trim() }.toSet()
val contributorsJson: String = JsonOutput.toJson(contributors)

val buildArchiveVersion: String = System.getProperty("build_type", "auto")
    .equals("final", true).ifTrue {
        version.toString()
    } ?: indraGit.commit()?.name().let {
    "Build_${it?.substring(0, 8) ?: "unknown"}"
}