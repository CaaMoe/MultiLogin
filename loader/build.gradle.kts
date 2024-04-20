import groovy.json.JsonOutput
import java.security.MessageDigest

val buildData: Map<String, String> by rootProject.extra

val digestConfiguration = configurations.register("digest")

fun DependencyHandler.digest(dependencyNotation: Any): Dependency? =
    add(digestConfiguration.name, dependencyNotation)

dependencies {
    compileOnly(project(":multilogin-api"))

    digest("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
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
    task.dependsOn(digestConfiguration)
    val digestedMap: MutableMap<String, String> = HashMap()

    digestConfiguration.get().resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        run {
            val dependency = artifact.moduleVersion.id
            val key = "${dependency.group}:${dependency.name}:${dependency.version}"
            digestedMap[key] = calculateDigest(artifact.file)
        }
    }

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