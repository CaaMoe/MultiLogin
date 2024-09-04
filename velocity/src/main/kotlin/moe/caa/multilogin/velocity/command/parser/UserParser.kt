package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.VelocityBrigadierMessage
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.toUUIDOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import java.util.*
import java.util.concurrent.CompletableFuture


object UserParser : ArgumentType<UserParser.ParseResult> {
    override fun parse(reader: StringReader): ParseResult {
        val cursor = reader.cursor
        try {
            val service = ServiceParser.parse(reader)
            if (!reader.canRead()) {
                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader)
            }
            reader.skip()
            val name = StringParser.parse(reader)
            val uuidOrNull = name.toUUIDOrNull()

            return MultiLoginVelocity.instance.database.useDatabase {
                val queryResult = if (uuidOrNull == null) {
                    UserDataTableV3.select(
                        UserDataTableV3.onlineUUID,
                        UserDataTableV3.onlineName,
                        UserDataTableV3.inGameProfileUUID,
                        UserDataTableV3.whitelist,
                    ).where {
                        UserDataTableV3.serviceId eq service.baseServiceSetting.serviceId and
                                (UserDataTableV3.onlineName.lowerCase() eq name.lowercase())
                    }
                } else {
                    UserDataTableV3.select(
                        UserDataTableV3.onlineUUID,
                        UserDataTableV3.onlineName,
                        UserDataTableV3.inGameProfileUUID,
                        UserDataTableV3.whitelist,
                    ).where {
                        UserDataTableV3.serviceId eq service.baseServiceSetting.serviceId and
                                (UserDataTableV3.onlineUUID eq uuidOrNull)
                    }
                }

                return@useDatabase queryResult
                    .limit(1).map {
                        ParseResult(
                            it[UserDataTableV3.onlineUUID],
                            service,
                            it[UserDataTableV3.onlineName],
                            it[UserDataTableV3.inGameProfileUUID],
                            it[UserDataTableV3.whitelist],
                        )
                    }.firstOrNull()
            } ?: throw SimpleCommandExceptionType(
                VelocityBrigadierMessage.tooltip(
                    MultiLoginVelocity.instance.message.message("command_parse_exception_user_not_found")
                        .replace("{service_name}", service.baseServiceSetting.serviceName)
                        .replace("{service_id}", service.baseServiceSetting.serviceId)
                        .replace("{name_or_uuid}", name)
                )
            ).create()
        } catch (throwable: Throwable) {
            reader.cursor = cursor
            throw throwable
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return ServiceParser.listSuggestions(context, builder)
    }

    data class ParseResult(
        val onlineUUID: UUID,
        val service: BaseService,
        val onlineName: String,
        val inGameProfileUUID: UUID?,
        val whitelist: Boolean
    )
}