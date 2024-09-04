package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.toUUIDOrNull
import java.util.*

object UUIDParser : ArgumentType<UUID> {
    override fun parse(reader: StringReader): UUID {
        val start = reader.cursor
        try {
            val parseString = StringParser.parse(reader)
            return parseString.toUUIDOrNull() ?: throw SimpleCommandExceptionType(
                VelocityBrigadierMessage.tooltip(
                    MultiLoginVelocity.instance.message.message("command_parse_exception_not_is_uuid")
                        .replace("{input}", parseString)
                )
            ).create()
        } catch (t: Throwable) {
            reader.cursor = start
            throw t
        }
    }

}