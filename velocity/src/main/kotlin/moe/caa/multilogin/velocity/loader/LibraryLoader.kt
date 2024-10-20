package moe.caa.multilogin.velocity.loader

import moe.caa.multilogin.velocity.util.calculateMD5
import moe.caa.multilogin.velocity.util.deleteRecursively
import moe.caa.multilogin.velocity.util.download
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.nio.file.Files
import kotlin.io.path.absolutePathString

class LibraryLoader(
    private val plugin: MultiLoginVelocity
) {
    companion object{
        private val map: MutableMap<String, List<Library>> = mutableMapOf()
        private val repositories = buildList {
            add("https://maven.aliyun.com/repository/public")
            add("https://repo1.maven.org/maven2")
            add("https://libraries.minecraft.net")
        }
    }

    fun load(group: String, classLoader: OpenURLClassLoader){
        val libraries = map[group]?: throw UnknownGroupLibraryException("Unknown group library $group")
        libraries.forEach { library ->
            load(library, classLoader)
        }
    }

    private fun load(library: Library, classLoader: OpenURLClassLoader){
        val path = library.getPath(plugin.dataDirectory.resolve("libraries"))
        if (Files.exists(path)) {
            kotlin.runCatching {
                if (path.calculateMD5() == library.md5) {
                    classLoader.addPath(path)
                    return
                }
                path.deleteRecursively()
                plugin.logger.warn("Failed to validate digest value of file ${path.absolutePathString()}, it will be re-downloaded.")
            }.onFailure {
                throw LibraryCalculationFailedException(library.localPath, it)
            }
        }

        // 下载
        var downloaded = false
        val exception = LibraryDownloadFailedException("Download failed: ${path.absolutePathString()}")
        for (repository in repositories) {
            kotlin.runCatching {
                val downloadUrl = "${repository.trim('/')}/${library.localPath}"
                plugin.logger.info("Downloading $downloadUrl")
                path.download(downloadUrl)
                downloaded = true
            }.onFailure {
                exception.addSuppressed(it)
            }
            if(downloaded) break
        }
        if(!downloaded) throw exception

        if (path.calculateMD5() != library.md5) {
            throw LibraryCalculationFailedException("The summary of the newly downloaded file is still incorrect: ${library.localPath}")
        }

        classLoader.addPath(path)
    }
}