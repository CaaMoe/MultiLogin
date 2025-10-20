import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("com.gradleup.shadow") version libs.versions.shadow apply false
}

tasks.jar { enabled = false }

subprojects {
    group = "moe.caa"
    version = "2.0.0+${getGitCommitID().substring(0, 8)}"

    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    configurations.register("extra")

    repositories {
        mavenCentral()

        file("${project.rootDir}/config/repositories.txt").readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { maven(it) }
    }

    tasks.build { finalizedBy(tasks.withType<ShadowJar>()) }
    tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

    tasks.withType<ShadowJar> {
        file("${project.rootDir}/config/relocations.txt").readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach {
                val parts = it.split("\\s+".toRegex())
                if (parts.size != 2) {
                    throw IllegalArgumentException("Invalid relocation entry: $it")
                }
                relocate(parts[0], parts[1])
            }

        manifest {
            attributes["Built-By"] = System.getProperty("user.name")
            attributes["Build-Jdk"] = System.getProperty("java.version")
            attributes["Build-OS"] =
                "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
            attributes["Build-Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())
            attributes["Build-Revision"] = getGitCommitID()
            attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
        }


        archiveFileName.set("${project.rootProject.name}-${project.name.uppercaseFirstChar()}.jar")
        doLast {
            val outputsFolder = File(project.rootProject.rootDir, "outputs").apply { mkdirs() }
            archiveFile.get().asFile.copyTo(File(outputsFolder, archiveFileName.get()), overwrite = true)
        }
    }

    tasks.jar { enabled = false }

    tasks.processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("${project.rootDir}/config/relocations.txt") { into("") }
        from("${project.rootDir}/config/repositories.txt") { into("") }


        doLast {
            destinationDir.mkdirs()
            File(destinationDir, "dependencies.txt").apply {
                writeText(
                    configurations["extra"].resolvedConfiguration.firstLevelModuleDependencies.joinToString("\n") {
                        "${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}"
                    }
                )
            }
        }
    }
}

fun Project.getGitCommitID(): String = runCatching {
    ProcessBuilder("git", "rev-parse", "HEAD")
        .directory(project.projectDir)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
}.getOrElse { "unknown" }