package moe.caa.multilogin.velocity.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

fun Path.resolves(others: Collection<String>) = buildPath(this) {
    others.forEach {
        resolve(it)
    }
}


fun Path.calculateSha1(): String {
    return fileReadAllBytes().calculateSha1()
}

fun Path.deleteRecursively() {
    if (Files.notExists(this)) return

    Files.walk(this).use { stream ->
        stream.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }
}

fun Path.fileReadAllBytes(): ByteArray = Files.readAllBytes(this)


fun Path.createDirectoriesIfNotExists() {
    if (!fileExists()) {
        Files.createDirectories(this)
    }
}

fun Path.saveToFile(bytes: ByteArray) {
    parent?.createDirectoriesIfNotExists()
    Files.write(this, bytes, StandardOpenOption.CREATE)
}

fun Path.fileExists() = Files.exists(this)

fun Path.loadHoconConfigurationNode(): ConfigurationNode {
    return HoconConfigurationLoader.builder().path(this).build().load()
}