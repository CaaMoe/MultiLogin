package moe.caa.multilogin.gradle.librarycollector

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import java.io.File
import java.security.MessageDigest


class LibraryCollector : Plugin<Project> {
    override fun apply(target: Project) {
        val summaryCalculateConfiguration = target.configurations.register("summaryCalculate")
        target.afterEvaluate {
            target.tasks.register("summaryCalculate") {
                dependsOn(summaryCalculateConfiguration)
                doLast {
                    val digestedMap: MutableMap<String, String> = HashMap()
                    configurations[summaryCalculateConfiguration.name].resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                        val dependency = artifact.moduleVersion.id
                        val key = "${dependency.group}:${dependency.name}:${dependency.version}"
                        digestedMap[key] = calculateDigest(artifact.file)
                    }

                    val file = target.layout.buildDirectory.file(".digested").get().asFile
                    file.parentFile?.mkdirs()
                    file.writeText(digestedMap.entries.joinToString("\n") { "${it.key}=${it.value}" })
                }
            }
        }
    }

    private fun calculateDigest(file: File): String {
        val bytes = file.readBytes()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}