package library.transform

import moe.caa.multilogin.velocity.library.Library
import moe.caa.multilogin.velocity.util.logDebug
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.io.path.outputStream

class LibraryTransformer(
    /**
     * 放处理过后的依赖的文件夹
     */
    private val librariesDirectory: Path,
    private val appendSuffixName: String,
    private vararg val transformers: ITransformer
) {

    fun transform(library: Library, libraryInput: Path): Path {
        return library.generateOutputJarPath(librariesDirectory, appendSuffixName).apply {
            outputStream().use { output ->
                JarOutputStream(output).use { jarOutput ->
                    val inputJar = JarFile(libraryInput.toFile())
                    inputJar.use { inputJarFile ->
                        for (entry in inputJar.entries()) {
                            val entryName = entry.name
                            val entryInputStream = inputJarFile.getInputStream(entry)

                            val jarEntry = JarEntry(entryName)
                            jarOutput.putNextEntry(jarEntry)

                            if (entryName.endsWith(".class", true) && !entry.isDirectory) {
                                val className = entryName.replace('/', '.').let { className ->
                                    className.substring(0, className.length - 6)
                                }

                                var classBytes: ByteArray
                                inputJarFile.getInputStream(entry).use { input ->
                                    ByteArrayOutputStream().use { output ->
                                        input.copyTo(output)
                                        output.flush()
                                        classBytes = output.toByteArray()
                                    }
                                }

                                transformers.forEach { transformer ->
                                    if (transformer.shouldTransform(className)) {
                                        logDebug("Transforming $className with $transformer")
                                        classBytes = transformer.transform(classBytes)
                                        logDebug("Transformed $className with $transformer")
                                    }
                                }
                                jarOutput.write(classBytes)
                            } else {
                                entryInputStream.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }
                }
            }
        }
    }
}