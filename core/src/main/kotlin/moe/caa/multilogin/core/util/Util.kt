package moe.caa.multilogin.core.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


fun UUID.toBytes(): ByteArray {
    val uuidBytes = ByteArray(16)
    ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(mostSignificantBits).putLong(leastSignificantBits)
    return uuidBytes
}

fun ByteArray.toUUID(): UUID {
    var i = 0
    var msl: Long = 0
    while (i < 8) {
        msl = (msl shl 8) or (this[i].toInt() and 0xFF).toLong()
        i++
    }
    var lsl: Long = 0
    while (i < 16) {
        lsl = (lsl shl 8) or (this[i].toInt() and 0xFF).toLong()
        i++
    }
    return UUID(msl, lsl)
}

fun String.toUUID() = UUID.fromString(replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5"))
