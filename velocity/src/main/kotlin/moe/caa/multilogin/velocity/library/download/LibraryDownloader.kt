package moe.caa.multilogin.velocity.library.download

import moe.caa.multilogin.velocity.library.Library
import moe.caa.multilogin.velocity.util.calculateSha1
import moe.caa.multilogin.velocity.util.logDebug
import moe.caa.multilogin.velocity.util.logWarn
import moe.caa.multilogin.velocity.util.saveToFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

/**
 * 依赖下载和处理器
 */
class LibraryDownloader(
    /**
     * 放依赖的文件夹
     */
    private val librariesDirectory: Path,
    /**
     * 依赖的mvn仓库链接
     */
    private val repositoryUrls: Set<String>
) {

    /**
     * 加载一个依赖, 返回依赖的文件路径
     */
    fun load(library: Library): Path {
        val expectSha1 = String(download(repositoryUrls.map { library.generateJarDownloadSha1URL(it) })).trim()

        val outputJarPath = library.generateOutputJarPath(librariesDirectory)
        if (Files.exists(outputJarPath)) {
            if (outputJarPath.calculateSha1() == expectSha1) {
                logDebug("The SHA-1 checksum verification for the cached file ${outputJarPath.toFile().absolutePath} is correct.")
                return outputJarPath
            } else {
                logWarn("SHA1 checksum verification failed for file ${outputJarPath.toFile().absolutePath}. Initiating re-download...")
            }
        }

        val downloadSource = download(repositoryUrls.map { library.generateJarDownloadURL(it) })
        downloadSource.saveToFile(outputJarPath)
        if (outputJarPath.calculateSha1() == expectSha1) {
            return outputJarPath
        }
        throw IOException("The re-downloaded file ${outputJarPath.toFile().absolutePath} still failed SHA1 checksum verification.")
    }

    private fun download(urls: List<String>): ByteArray {
        val exception = IOException("Error while downloading.")
        urls.forEach { url ->
            val bytes = runCatching {
                download(url)
            }.onFailure {
                exception.addSuppressed(it)
            }.getOrNull()
            if (bytes != null) {
                return bytes
            }
        }
        throw exception
    }

    private fun download(url: String): ByteArray {
        logDebug("Downloading $url")
        val currentTimeMillis = System.currentTimeMillis()
        URL(url).openStream().use {
            ByteArrayOutputStream().use { output ->
                it.copyTo(output)
                output.flush()
                logDebug("Downloaded $url, took ${System.currentTimeMillis() - currentTimeMillis} ms.")
                return output.toByteArray()
            }
        }
    }
}