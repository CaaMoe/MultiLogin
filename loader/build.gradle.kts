import groovy.json.JsonOutput
import java.security.MessageDigest

val buildData: Map<String, String> by rootProject.extra

dependencies {
    compileOnly(project(":multilogin-api"))
}

tasks.processResources {
    extraLibrariesSummary(this)

    layout.buildDirectory.file("builddata").get().asFile.apply {
        parentFile?.mkdirs()
    }.writeText(
        JsonOutput.toJson(buildData)
    )
    from(layout.buildDirectory) {
        include("builddata")
    }
}

fun extraLibrariesSummary(task: ProcessResources) {
    val digestedMap: MutableMap<String, String> = HashMap()

    // todo collect libraries

    val file = layout.buildDirectory.file(".digested").get().asFile
    file.parentFile?.mkdirs()
    file.writeText(digestedMap.entries.joinToString("\n") { "${it.key}=${it.value}" })

    task.from(layout.buildDirectory) {
        include(".digested")
    }
}

fun calculateDigest(file: File): String {
    val bytes = file.readBytes()
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}