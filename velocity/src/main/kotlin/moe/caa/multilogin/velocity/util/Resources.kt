package moe.caa.multilogin.velocity.util

import org.spongepowered.configurate.ConfigurationNode
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream

sealed class ResourceException(message: String, cause: Throwable? = null): IOException(message, cause)

class ResourceNotFoundException(message: String, cause: Throwable? = null): ResourceException(message, cause)
class NodeUndefinedException(message: String, cause: Throwable? = null): ResourceException(message, cause)


fun Any.getResource(resource: String) = javaClass.getResourceAsStream("/$resource")?: throw ResourceNotFoundException("Resource '$resource' not found.")

fun Any.saveDefaultResource(directory: Path, resource: String, cover: Boolean = false): Path {
    val resolved = directory.resolves(resource.trim('/').split("/"))
    val exist = resolved.exists()
    if(!cover && exist) return resolved
    resolved.parent?.createDirectoriesIfNotExist()

    getResource(resource).use { input ->
        resolved.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return resolved
}

fun ConfigurationNode.requireNode(vararg path: String): ConfigurationNode {
    return node(*path)?: throw NodeUndefinedException("Node ${
        if(path().size() == 0){
            path.joinToString(".")
        } else {
            path().joinToString(".") + "." + path.joinToString(".")
        }
    } undefined.")
}