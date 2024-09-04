package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.Player
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.toUUIDOrNull
import kotlin.jvm.optionals.getOrNull


object PlayerParser : ArgumentType<Player> {
    override fun parse(reader: StringReader): Player {
        val start = reader.cursor
        try {
            val result = StringParser.parse(reader)

            result.toUUIDOrNull()?.apply {
                return MultiLoginVelocity.instance.proxyServer.getPlayer(this).getOrNull()
                    ?: throw SimpleCommandExceptionType(
                        VelocityBrigadierMessage.tooltip(
                            MultiLoginVelocity.instance.message.message("command_parse_exception_player_uuid_not_found")
                                .replace("{input}", this)
                        )
                    ).create()
            }
            return MultiLoginVelocity.instance.proxyServer.getPlayer(result).getOrNull()
                ?: throw SimpleCommandExceptionType(
                    VelocityBrigadierMessage.tooltip(
                        MultiLoginVelocity.instance.message.message("command_parse_exception_player_name_not_found")
                            .replace("{input}", result)
                    )
                ).create()
        } catch (t: Throwable) {
            reader.cursor = start
            throw t
        }
    }
}