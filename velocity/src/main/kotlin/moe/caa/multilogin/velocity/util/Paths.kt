package moe.caa.multilogin.velocity.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.createDirectories

fun Path.resolves(others: Collection<String>): Path {
    var resolved = this
    for (path in others) {
        resolved = resolved.resolve(path)
    }
    return resolved
}

fun Path.calculateMD5(): String{
    val bytes = Files.readAllBytes(this)
    val digest = MessageDigest.getInstance("MD5")

    return digest.digest(bytes).joinToString("") { "%02x".format(it) }
}

fun Path.deleteRecursively() {
    if (Files.notExists(this)) return

    Files.walk(this).use { stream ->
        stream.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }
}

fun Path.download(url: String){
    URL(url).openStream().use { input ->
        Files.copy(input, this, StandardCopyOption.REPLACE_EXISTING)
    }
}

fun Path.createDirectoriesIfNotExist(){
    if (!Files.exists(this)) {
        createDirectories()
    }
}

fun Path.loadHoconConfigurationNode(): ConfigurationNode {
    return HoconConfigurationLoader.builder().path(this).build().load()
}
