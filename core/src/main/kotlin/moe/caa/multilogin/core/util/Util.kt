package moe.caa.multilogin.core.util

import java.util.*


fun String.toUUID(): UUID =
    UUID.fromString(replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5"))

fun String.toUUIDOrNull(): UUID? {
    return try {
        this.toUUID()
    } catch (ignored: Throwable) {
        null
    }
}

fun String.camelCaseToUnderscore(): String {
    return Regex("([a-z])([A-Z])").replace(this) {
        "${it.groupValues[1]}_${it.groupValues[2].lowercase()}"
    }.lowercase()
}