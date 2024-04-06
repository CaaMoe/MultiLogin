package moe.caa.multilogin.core.command.parser

import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import org.incendo.cloud.caption.CaptionVariable
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class ServiceIdArgumentParser<C> : ArgumentParser<C, BaseService>, BlockingSuggestionProvider.Strings<C> {
    companion object {
        fun <C> serviceIdParser(): ParserDescriptor<C, BaseService> =
            ParserDescriptor.of(ServiceIdArgumentParser(), BaseService::class.java)

        fun <C> serviceIdComponent(): CommandComponent.Builder<C, BaseService> =
            CommandComponent.builder<C, BaseService>().parser(serviceIdParser())
    }

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<BaseService> {
        val serviceId = commandInput.readInteger()

        val service = GeneralConfiguration.services[serviceId]
        if (service != null) return ArgumentParseResult.success(service)
        return ArgumentParseResult.failure(ServiceIdParseException(serviceId, commandContext))
    }

    class ServiceIdParseException(private val serviceId: Int, context: CommandContext<*>) : ParserException(
        ServiceIdArgumentParser::class.java, context,
        StandardCaptionKeys.EXCEPTION_UNEXPECTED,
        CaptionVariable.of("input", serviceId.toString())
    )

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return GeneralConfiguration.services.map { it.key.toString() }
    }
}