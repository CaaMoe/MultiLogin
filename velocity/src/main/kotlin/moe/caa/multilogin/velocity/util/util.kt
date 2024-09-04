package moe.caa.multilogin.velocity.util

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.Player
import moe.caa.multilogin.velocity.main.InGameData
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import net.kyori.adventure.text.Component
import java.io.File
import java.lang.reflect.AccessibleObject
import java.sql.SQLIntegrityConstraintViolationException
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

fun Throwable.logCausedSQLIntegrityConstraintViolationOrThrow(throwable: Throwable = this) {
    if (cause !is SQLIntegrityConstraintViolationException) {
        throw throwable
    }
    MultiLoginVelocity.instance.logDebug(this.message, this)
}

fun <T : ArgumentBuilder<CommandSource, T>> ArgumentBuilder<CommandSource, T>.handler(handle: CommandContext<CommandSource>.() -> Unit): ArgumentBuilder<CommandSource, T> {
    executes {
        handle.invoke(it)
        return@executes 0
    }
    return this
}

fun <T : ArgumentBuilder<CommandSource, T>> ArgumentBuilder<CommandSource, T>.permission(
    permission: String
): ArgumentBuilder<CommandSource, T> {
    requires {
        it.hasPermission(permission)
    }
    return this
}

fun ArgumentBuilder<CommandSource, *>.thenLiteral(
    literal: String,
    literalBuilder: ArgumentBuilder<CommandSource, *>.() -> Unit
): ArgumentBuilder<*, *> {

    val builder = LiteralArgumentBuilder.literal<CommandSource>(literal)
    literalBuilder.invoke(builder)

    return this.then(builder)
}

fun ArgumentBuilder<CommandSource, *>.thenArgument(
    argument: String,
    argumentType: ArgumentType<*>,
    argumentBuilder: ArgumentBuilder<CommandSource, *>.() -> Unit
): ArgumentBuilder<*, *> {

    val builder: ArgumentBuilder<CommandSource, *> = RequiredArgumentBuilder.argument(argument, argumentType)
    argumentBuilder.invoke(builder)

    return this.then(builder)
}

fun ArgumentBuilder<CommandSource, *>.thenArgumentOptional(
    argument: String,
    argumentType: ArgumentType<*>,
    argumentBuilder: ArgumentBuilder<CommandSource, *>.() -> Unit
): ArgumentBuilder<*, *> {

    val builder: ArgumentBuilder<CommandSource, *> = RequiredArgumentBuilder.argument(argument, argumentType)
    argumentBuilder.invoke(builder)
    argumentBuilder.invoke(this)

    return this.then(builder)
}

fun <V> CommandContext<*>.getArgumentOrNull(name: String, clazz: Class<V>) =
    if (arguments.containsKey(name)) {
        getArgument(name, clazz)
    } else {
        null
    }

fun CommandContext<CommandSource>.player(): Player {
    if (source is Player) {
        return source as Player
    }

    throw SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(
            MultiLoginVelocity.instance.message.message("command_require_player_execute")
        )
    ).create()
}

fun Player.commandGetUserData(): InGameData.InGameEntry {
    return InGameData.findByPlayer(this) ?: throw SimpleCommandExceptionType(
        VelocityBrigadierMessage.tooltip(
            MultiLoginVelocity.instance.message.message("command_require_player_user_data")
        )
    ).create()
}