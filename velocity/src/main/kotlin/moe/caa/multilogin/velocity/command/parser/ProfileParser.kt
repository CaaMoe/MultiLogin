package moe.caa.multilogin.velocity.command.parser

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import moe.caa.multilogin.velocity.database.ProfileTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.toUUIDOrNull
import org.jetbrains.exposed.sql.lowerCase
import java.util.*

object ProfileParser : ArgumentType<ProfileParser.ParseResult> {
    override fun parse(reader: StringReader): ParseResult {
        val cursor = reader.cursor
        try {
            val name = StringParser.parse(reader)
            val uuidOrNull = name.toUUIDOrNull()

            return MultiLoginVelocity.instance.database.useDatabase {
                val queryResult = if (uuidOrNull == null) {
                    ProfileTableV3.select(
                        ProfileTableV3.id,
                        ProfileTableV3.currentUserNameOriginal
                    ).where {
                        (ProfileTableV3.currentUserNameOriginal.lowerCase() eq name.lowercase())
                    }
                } else {
                    ProfileTableV3.select(
                        ProfileTableV3.id,
                        ProfileTableV3.currentUserNameOriginal
                    ).where {
                        ProfileTableV3.id eq uuidOrNull
                    }
                }

                return@useDatabase queryResult
                    .limit(1).map {
                        ParseResult(
                            it[ProfileTableV3.id].value,
                            it[ProfileTableV3.currentUserNameOriginal],
                        )
                    }.firstOrNull()
            } ?: throw SimpleCommandExceptionType(
                VelocityBrigadierMessage.tooltip(
                    MultiLoginVelocity.instance.message.message("command_parse_exception_profile_not_found")
                        .replace("{name_or_uuid}", name)
                )
            ).create()
        } catch (throwable: Throwable) {
            reader.cursor = cursor
            throw throwable
        }
    }

    data class ParseResult(
        val profileUUID: UUID,
        val profileName: String
    )
}