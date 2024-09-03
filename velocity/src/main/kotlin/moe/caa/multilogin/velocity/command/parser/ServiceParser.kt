package moe.caa.multilogin.velocity.command.parser

import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class ServiceParser<C> : ArgumentParser<C, BaseService>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<BaseService> {
        val argument = commandInput.peekString()

        return ArgumentParseResult.success(
            MultiLoginVelocity.instance.config.serviceMap[
                argument.toIntOrNull()
                    ?: return ArgumentParseResult.failure(
                        CommandHandler.CommandParseException(
                            MultiLoginVelocity.instance.message.message("command_parse_exception_service_not_a_int")
                                .replace("{input}", argument)
                        )
                    )
            ] ?: return ArgumentParseResult.failure(
                CommandHandler.CommandParseException(
                    MultiLoginVelocity.instance.message.message("command_parse_exception_service_not_found")
                        .replace("{input}", argument)
                )
            )
        )
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): MutableIterable<String> {
        return MultiLoginVelocity.instance.config.serviceMap.keys.map { it.toString() }.toMutableList()
    }
}