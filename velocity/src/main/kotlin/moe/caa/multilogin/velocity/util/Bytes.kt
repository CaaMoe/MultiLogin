package moe.caa.multilogin.velocity.util

import java.math.BigInteger
import java.nio.file.Path
import java.security.MessageDigest

fun ByteArray.calculateSha1(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    return BigInteger(1, digest.digest(this)).toString(16).padStart(40, '0')
}

fun ByteArray.saveToFile(path: Path) = path.saveToFile(this)
