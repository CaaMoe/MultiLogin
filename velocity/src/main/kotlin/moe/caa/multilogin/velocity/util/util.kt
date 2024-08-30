package moe.caa.multilogin.velocity.util

import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import net.kyori.adventure.text.Component
import java.io.File
import java.lang.reflect.AccessibleObject
import java.util.*


/**
 * 给定路径获得 Jar 包内资源
 */
fun getResource(resource: String) = MultiLoginVelocity::class.java
    .getResourceAsStream("/$resource") ?: throw Exception("Resource '$resource' not found")

/**
 * 保存 Jar 包内资源到指定文件夹中
 * @param resource 资源路径
 * @param cover 如果指定目标存在, 是否覆盖
 */
fun saveDefaultResource(folder: File, resource: String, cover: Boolean = false): File {
    val file = File(folder, resource)
    val exist = file.exists()

    if (!cover && exist) return file

    file.parentFile?.mkdirs()
    getResource(resource).use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file
}


fun <T : AccessibleObject> T.access(): T {
    isAccessible = true
    return this
}

fun <T> Class<T>.enumConstant(name: String): T {
    return enumConstants.first { (it as Enum<*>).name == name }
}

// 大驼峰转下划线
fun String.camelCaseToUnderscore(): String {
    return Regex("([a-z])([A-Z])").replace(this) {
        "${it.groupValues[1]}_${it.groupValues[2].lowercase()}"
    }.lowercase()
}

fun String.componentText() = Component.text(this)

fun String.toUUIDOrNull(): UUID? {
    try {
        return UUID.fromString(
            this.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
                "$1-$2-$3-$4-$5"
            )
        )
    } catch (ignored: java.lang.Exception) {
    }
    return null
}