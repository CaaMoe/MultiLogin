package library.load

import java.net.URLClassLoader
import java.nio.file.Path

class LibraryLoader(parent: ClassLoader) : URLClassLoader(arrayOf(), parent) {
    fun addLibraryFromPath(path: Path) {
        addURL(path.toUri().toURL())
    }
}