package moe.caa.multilogin.velocity.library

import moe.caa.multilogin.velocity.util.buildPath
import java.nio.file.Path

data class Library(
    val groupId: String,
    val artifactId: String,
    val version: String
) {
    private val outputJarFilename = "${artifactId}-${version}.jar"

    fun generateOutputJarPath(directory: Path, appendName: String = "") = buildPath(directory) {
        groupId.split(".").forEach { resolve(it) }
        resolve(artifactId)
        resolve(version)
        resolve(outputJarFilename + appendName)
    }

    // repositoryUrl/ggg/rrr/ooo/uuu/ppp/artifactId/version/artifactId-version.jar
    // https://www.example.com/repository/com/google/gson/gson/gsonversion/gson-gsonversion.jar
    fun generateJarDownloadURL(repositoryUrl: String) = buildString {
        append(repositoryUrl.trim('/').trim('\\').replace("\\", "/"))
        append('/')

        groupId.split(".").forEach {
            append(it)
            append('/')
        }

        append(artifactId)
        append('/')

        append(version)
        append('/')

        append(outputJarFilename)
    }

    fun generateJarDownloadSha1URL(repositoryUrl: String) = buildString {
        append(generateJarDownloadURL(repositoryUrl))
        append(".sha1")
    }
}
