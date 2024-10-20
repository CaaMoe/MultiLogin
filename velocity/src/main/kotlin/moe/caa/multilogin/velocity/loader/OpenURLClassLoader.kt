package moe.caa.multilogin.velocity.loader

import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path

class OpenURLClassLoader(parent: ClassLoader): URLClassLoader(arrayOf(), parent) {
    companion object {
        init {
            ClassLoader.registerAsParallelCapable()
        }
    }
    fun addPath(path: Path) = addURL(path.toUri().toURL())
}