import groovy.json.JsonOutput
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

val contributors = project.rootProject.file("contributors").readLines().map { it.trim() }.toSortedSet()
val contributorsJson: String = JsonOutput.toJson(contributors)

val buildArchiveVersion: String = System.getProperty("build_type", "auto")
    .equals("final", true).ifTrue {
        project.properties["plugin_version"] as String


    } ?: indraGit.commit()?.name().let {
    "build_${it?.substring(0, 8) ?: "unknown"}"
}

val buildData by extra(
    fun(): Map<String, String> = mapOf(
        "Build-By" to System.getProperty("user.name"),
        "Build-Timestamp" to System.currentTimeMillis().toString(),
        "Build-Datetime" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
        "Build-Revision" to (indraGit.commit()?.name ?: "unknown"),
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
        }",

        "Plugin-Version" to buildArchiveVersion,
        "Contributors-Json" to contributorsJson,
        "Contributors" to contributors.joinToString(),
        "Build-Type" to System.getProperty("build_type", "auto"),
        "Build-Revision" to (indraGit.commit()?.name ?: "unknown"),
    )
)

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinserialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.git)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

allprojects {
    group = "moe.caa"

    repositories {
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "net.kyori.indra.git")
    apply(plugin = "com.github.johnrengelman.shadow")

    java.sourceCompatibility = rootProject.java.sourceCompatibility
    java.targetCompatibility = rootProject.java.targetCompatibility

    tasks.shadowJar {
        archiveAppendix = "${rootProject.name}-${project.name.substring(rootProject.name.length + 1)}"
        archiveBaseName = ""
        archiveVersion = buildArchiveVersion
        archiveClassifier = ""

        manifest {
            attributes(buildData)
        }
    }

    tasks.named("build") {
        finalizedBy(tasks.named("shadowJar"))
    }

    tasks.processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        Properties().apply {
            putAll(buildData)
        }.store(OutputStreamWriter(layout.buildDirectory.file("builddata").get().asFile.apply {
            parentFile?.mkdirs()
        }.outputStream(), StandardCharsets.UTF_8), "MultiLogin build data.")

        from(layout.buildDirectory) {
            include("builddata")
        }
    }
}

