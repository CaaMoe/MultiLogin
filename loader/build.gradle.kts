import groovy.json.JsonOutput
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("library-collector")
}

dependencies {
    compileOnly(project(":multilogin-api"))
}

tasks.processResources {
    dependsOn("summaryCalculate")

    from(layout.buildDirectory) {
        include(".digested")
    }


    val tokens = mapOf(
        "version" to outputArchiveVersion,
        "build_by" to System.getProperty("user.name"),
        "contributors_json" to contributorsJson,
        "contributors" to contributors.joinToString(),
        "build_type" to System.getProperty("build_type", "auto"),
        "build_revision" to (indraGit.commit()?.name ?: "unknown"),
        "build_timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
    )

    filter(
        ReplaceTokens::class, mapOf(
            "tokens" to tokens,
            "beginToken" to "@",
            "endToken" to "@"
        )
    )

    layout.buildDirectory.file("builddata").get().asFile.apply {
        parentFile?.mkdirs()
    }.writeText(
        JsonOutput.toJson(tokens)
    )
    from(layout.buildDirectory) {
        include("builddata")
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