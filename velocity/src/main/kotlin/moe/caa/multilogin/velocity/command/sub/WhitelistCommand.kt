package moe.caa.multilogin.velocity.command.sub

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.command.*
import moe.caa.multilogin.velocity.command.parser.ServiceParser
import moe.caa.multilogin.velocity.command.parser.StringParser
import moe.caa.multilogin.velocity.command.parser.UserParser
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.database.CacheWhitelistTableV2
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.InGameData
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class WhitelistCommand(
    private val handler: CommandHandler
) {
    fun register() {
        handler.register {
            thenLiteral("whitelist") {
                permission(COMMAND_WHITELIST_BASE)
                thenLiteral("addCache") {
                    permission(COMMAND_WHITELIST_ADD_CACHE)
                    thenArgumentOptional("service", ServiceParser) {
                        thenArgument("name_or_uuid", StringParser) {
                            handler { handleAddCache(this) }
                        }
                    }
                }
                thenLiteral("addSpecific") {
                    permission(COMMAND_WHITELIST_ADD_SPECIFIC)
                    thenArgument("user", UserParser) {
                        handler { handleAddSpecific(this) }
                    }
                }
                thenLiteral("removeCache") {
                    permission(COMMAND_WHITELIST_REMOVE_CACHE)
                    thenArgumentOptional("service", ServiceParser) {
                        thenArgument("name_or_uuid", StringParser) {
                            handler { handleRemoveCache(this) }
                        }
                    }
                }
                thenLiteral("removeSpecific") {
                    permission(COMMAND_WHITELIST_REMOVE_SPECIFIC)
                    thenArgument("user", UserParser) {
                        handler { handleRemoveSpecific(this) }
                    }
                }
                thenLiteral("clearCache") {
                    permission(COMMAND_WHITELIST_CLEAR_CACHE)
                    handler {
                        handler.needConfirm(
                            source, MultiLoginVelocity.instance.message.message(
                                "command_execute_whitelist_clear_cache_confirm_aim"
                            )
                        ) {
                            handleClearCache(this)
                        }
                    }
                }
            }
        }
    }

    private fun handleClearCache(context: CommandContext<CommandSource>) {
        val count = MultiLoginVelocity.instance.database.useDatabase {
            CacheWhitelistTableV2.deleteAll()
        }

        if (count > 0) {
            context.source.sendMessage(
                MultiLoginVelocity.instance.message.message("command_execute_whitelist_clear_cache_success")
                    .replace("{count}", count)
            )
        } else {
            context.source.sendMessage(
                MultiLoginVelocity.instance.message.message("command_execute_whitelist_clear_cache_nothing")
            )
        }
    }

    private fun handleRemoveSpecific(context: CommandContext<CommandSource>) {
        val user = context.getArgument("user", UserParser.ParseResult::class.java)

        if (!user.whitelist) {
            context.source.sendMessage(
                MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_specified_repeat")
                    .replace("{service_name}", user.service.baseServiceSetting.serviceName)
                    .replace("{service_id}", user.service.baseServiceSetting.serviceId)
                    .replace("{user_uuid}", user.onlineUUID)
                    .replace("{user_name}", user.onlineName)
            )
            return
        }

        if (user.service.baseServiceSetting.whitelist) {
            InGameData.findByUser(
                user.service.baseServiceSetting.serviceId, user.onlineUUID
            )?.connectedPlayer?.disconnect(
                MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_specified_success_kick_message")
            )
        }
        // todo 踢掉当前在线的玩家
        MultiLoginVelocity.instance.database.useDatabase {
            UserDataTableV3.update({
                UserDataTableV3.serviceId eq user.service.baseServiceSetting.serviceId and
                        (UserDataTableV3.onlineUUID eq user.onlineUUID)
            }) {
                it[whitelist] = false
            }
        }

        context.source.sendMessage(
            MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_specified_success")
                .replace("{service_name}", user.service.baseServiceSetting.serviceName)
                .replace("{service_id}", user.service.baseServiceSetting.serviceId)
                .replace("{user_uuid}", user.onlineUUID)
                .replace("{user_name}", user.onlineName)
        )
    }

    private fun handleRemoveCache(context: CommandContext<CommandSource>) {
        val nameOrUuid = context.getArgument("name_or_uuid", String::class.java)
        val baseService = context.getArgumentOrNull("service", BaseService::class.java)

        if (MultiLoginVelocity.instance.database.useDatabase {
                CacheWhitelistTableV2.deleteWhere {
                    serviceId eq (baseService?.baseServiceSetting?.serviceId ?: -1) and (
                            target.lowerCase() eq nameOrUuid.lowercase())
                }
            } > 0) {
            if (baseService == null) {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_cache_success_unspecified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                )
            } else {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_cache_success_specified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                        .replace("{service_name}", baseService.baseServiceSetting.serviceName)
                        .replace("{service_id}", baseService.baseServiceSetting.serviceId)
                )
            }
        } else {
            if (baseService == null) {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_cache_repeat_unspecified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                )
            } else {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_remove_cache_repeat_specified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                        .replace("{service_name}", baseService.baseServiceSetting.serviceName)
                        .replace("{service_id}", baseService.baseServiceSetting.serviceId)
                )
            }
        }
    }

    private fun handleAddSpecific(context: CommandContext<CommandSource>) {
        val user = context.getArgument("user", UserParser.ParseResult::class.java)

        if (user.whitelist) {
            context.source.sendMessage(
                MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_specified_repeat")
                    .replace("{service_name}", user.service.baseServiceSetting.serviceName)
                    .replace("{service_id}", user.service.baseServiceSetting.serviceId)
                    .replace("{user_uuid}", user.onlineUUID)
                    .replace("{user_name}", user.onlineName)
            )
            return
        }
        MultiLoginVelocity.instance.database.useDatabase {
            UserDataTableV3.update({
                UserDataTableV3.serviceId eq user.service.baseServiceSetting.serviceId and
                        (UserDataTableV3.onlineUUID eq user.onlineUUID)
            }) {
                it[whitelist] = true
            }
        }

        context.source.sendMessage(
            MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_specified_success")
                .replace("{service_name}", user.service.baseServiceSetting.serviceName)
                .replace("{service_id}", user.service.baseServiceSetting.serviceId)
                .replace("{user_uuid}", user.onlineUUID)
                .replace("{user_name}", user.onlineName)
        )
    }

    private fun handleAddCache(context: CommandContext<CommandSource>) {
        val nameOrUuid = context.getArgument("name_or_uuid", String::class.java)
        val baseService = context.getArgumentOrNull("service", BaseService::class.java)
        try {
            MultiLoginVelocity.instance.database.useDatabase {
                CacheWhitelistTableV2.insert {
                    it[serviceId] = baseService?.baseServiceSetting?.serviceId ?: -1
                    it[target] = nameOrUuid
                }
            }
            if (baseService == null) {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_cache_success_unspecified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                )
            } else {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_cache_success_specified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                        .replace("{service_name}", baseService.baseServiceSetting.serviceName)
                        .replace("{service_id}", baseService.baseServiceSetting.serviceId)
                )
            }
        } catch (e: ExposedSQLException) {
            e.logCausedSQLIntegrityConstraintViolationOrThrow()
            if (baseService == null) {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_cache_repeat_unspecified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                )
            } else {
                context.source.sendMessage(
                    MultiLoginVelocity.instance.message.message("command_execute_whitelist_add_cache_repeat_specified_service_id")
                        .replace("{name_or_uuid}", nameOrUuid)
                        .replace("{service_name}", baseService.baseServiceSetting.serviceName)
                        .replace("{service_id}", baseService.baseServiceSetting.serviceId)
                )
            }
        }
    }
}