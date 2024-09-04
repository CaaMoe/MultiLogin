package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.VelocityBrigadierMessage
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import java.util.*
import java.util.concurrent.CompletableFuture


object ServiceParser : ArgumentType<BaseService> {
    override fun parse(reader: StringReader): BaseService {
        val start = reader.cursor
        try {
            val result = reader.readInt()
            return MultiLoginVelocity.instance.config.serviceMap[result] ?: throw SimpleCommandExceptionType(
                VelocityBrigadierMessage.tooltip(
                    MultiLoginVelocity.instance.message.message("command_parse_exception_service_not_found")
                        .replace("{input}", result.toString())
                )
            ).create()
        } catch (t: Throwable) {
            reader.cursor = start
            throw t
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        MultiLoginVelocity.instance.config.serviceMap.forEach { (key, _) ->
            if ((key.toString() + "").startsWith(builder.remaining.lowercase(Locale.getDefault()))) {
                builder.suggest(key)
            }
        }
        return builder.buildFuture()
    }
}