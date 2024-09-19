package moe.caa.multilogin.velocity.loader.library

import moe.caa.multilogin.velocity.loader.LibraryLoadFailedException
import moe.caa.multilogin.velocity.loader.OpenURLClassLoader
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.getResource
import moe.caa.multilogin.velocity.util.hexToByteArray
import java.io.File
import java.io.InputStreamReader

class LibraryManager(
    val librariesFolder: File,
) {
    private lateinit var collectLibraries: Map<String, List<Library>>

    fun init(){
        val collectLibraries = HashMap<String, MutableList<Library>>()

        val digest = HashMap<String, ByteArray>().apply {
            getResource(".group_libraries_digested").use { stream ->
                stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.forEach { line ->
                        if (line.trim().isEmpty()) return@useLines
                        val strings = line.split("=")
                        this[strings[0]] = strings[1].hexToByteArray()
                    }
                }
            }
        }


        getResource(".group_libraries").use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                var currentGroup = "root"
                reader.forEachLine { oLine ->
                    val line = oLine.trim()
                    if(line.isEmpty()) return@forEachLine

                    if(line.startsWith("#")) {
                        currentGroup = line.substring(1).trim()
                        return@forEachLine
                    }

                    (collectLibraries).computeIfAbsent(currentGroup) { mutableListOf() }.add(
                        line.split(":").let {
                            Library(it[0], it[1], it[2], digest[line]?: throw RuntimeException("No MD5 hash was collected for $line."))
                        }
                    )
                }
            }
        }

        this.collectLibraries = collectLibraries
    }

    fun loadLibrary(group: String, openURLClassLoader: OpenURLClassLoader){
        val libraries = collectLibraries[group] ?: throw IllegalArgumentException("group $group not found.")
        kotlin.runCatching {
            libraries.forEach { library ->
                val file = library.getFile(librariesFolder)
                if (file.exists()) {
                    if (library.md5Check(file)) {
                        openURLClassLoader.addURL(file.toURI().toURL())
                        return@forEach
                    }
                }

                if(file.exists()){
                    MultiLoginVelocity.instance.logger.warn("The MD5 hash verification of file ${file.absolutePath} failed, download again.")
                }
                file.parentFile?.mkdirs()
                // redownload

                // recheck
                if (library.md5Check(file)) {
                    throw RuntimeException("The MD5 hash of the re-downloaded file ${file.absolutePath} still fails verification.")
                }
                openURLClassLoader.addURL(file.toURI().toURL())
            }
        }.onFailure {
            throw LibraryLoadFailedException("Failed to load library group $group.", it)
        }
    }
}