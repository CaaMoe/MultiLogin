package moe.caa.multilogin.velocity.command.sub

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.auth.OnlineGameData
import moe.caa.multilogin.velocity.command.COMMAND_LINK_ACCEPT
import moe.caa.multilogin.velocity.command.COMMAND_LINK_TO
import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.command.parser.ProfileParser
import moe.caa.multilogin.velocity.command.parser.StringParser
import moe.caa.multilogin.velocity.command.parser.UserParser
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LinkCommand(
    private val handler: CommandHandler
) {

    // 连接发起者和连接数据
    private val dataMap = ConcurrentHashMap<UUID, LinkData>()

    fun register() {
        handler.register {
            thenLiteral("link") {
                thenLiteral("to") {
                    permission(COMMAND_LINK_TO)
                    thenArgument("profile", ProfileParser) {
                        handler { handleLinkTo(this) }
                    }
                }
                thenLiteral("accept") {
                    permission(COMMAND_LINK_ACCEPT)
                    thenArgument("user", UserParser) {
                        thenArgument("verify_code", StringParser) {
                            handler { handleLinkAccept(this) }
                        }
                    }
                }
            }
        }
    }

    private fun handleLinkAccept(context: CommandContext<CommandSource>) {
        val requester = context.getArgument("user", UserParser.ParseResult::class.java)
        val verifyCode = context.getArgument("verify_code", String::class.java)

        dataMap.values.removeIf { it.isExpired() }

        val linkData = dataMap.values.firstOrNull {
            it.requesterUUID == requester.onlineUUID
                    && it.requestServiceId == requester.service.baseServiceSetting.serviceId
                    && it.targetProfileUUID == context.player.uniqueId
        }

        if (linkData == null) {
            context.source.sendMessage(handler.plugin.message.message("command_execute_link_accept_nothing"))
            return
        }
        dataMap.remove(requester.onlineUUID)
        if (!verifyCode.equals(linkData.verifyCode)) {
            context.source.sendMessage(handler.plugin.message.message("command_execute_link_accept_verify_code_error"))
            return
        }

        handler.needConfirm(
            context.source, handler.plugin.message.message("command_execute_link_accept_confirm_aim")
                .replace("{service_name}", requester.service.baseServiceSetting.serviceName)
                .replace("{service_id}", requester.service.baseServiceSetting.serviceId)
                .replace("{user_uuid}", requester.onlineUUID)
                .replace("{user_name}", requester.onlineName)
                .replace("{profile_uuid}", context.player.uniqueId)
                .replace("{profile_name}", context.player.username)
        ) {
            MultiLoginVelocity.instance.database.useDatabase {
                UserDataTableV3.update({
                    UserDataTableV3.serviceId eq linkData.requestServiceId and (
                            UserDataTableV3.onlineUUID eq linkData.requesterUUID
                            )
                }) {
                    it[inGameProfileUUID] = linkData.targetProfileUUID
                }
            }

            MultiLoginVelocity.instance.findOnlineDataByUser(
                linkData.requestServiceId, linkData.requesterUUID
            )?.lazyPlayer?.value?.disconnect(
                MultiLoginVelocity.instance.message.message("command_execute_link_accepted_kick_msg")
                    .replace("{profile_uuid}", context.player.uniqueId)
                    .replace("{profile_name}", context.player.username)
            )

            context.source.sendMessage(
                handler.plugin.message.message("command_execute_link_accepted")
                    .replace("{service_name}", requester.service.baseServiceSetting.serviceName)
                    .replace("{service_id}", requester.service.baseServiceSetting.serviceId)
                    .replace("{user_uuid}", requester.onlineUUID)
                    .replace("{user_name}", requester.onlineName)
                    .replace("{profile_uuid}", context.player.uniqueId)
                    .replace("{profile_name}", context.player.username)
            )
        }
    }

    private fun handleLinkTo(context: CommandContext<CommandSource>) {
        val userData = context.player.gameData
        val parseResult = context.getArgument("profile", ProfileParser.ParseResult::class.java)

        if (userData == null || userData !is OnlineGameData) {
            context.source.sendMessage(handler.plugin.message.message("command_execute_link_to_no_user_data"))
            return
        }
        if (userData.inGameProfile.uuid.equals(parseResult.profileName)) {
            context.source.sendMessage(
                handler.plugin.message.message("command_execute_link_to_nothing")
                    .replace("{target_profile_name}", parseResult.profileName)
                    .replace("{target_profile_uuid}", parseResult.profileUUID)
            )
            return
        }

        handler.needConfirm(
            context.source, handler.plugin.message.message("command_execute_link_to_confirm_aim")
                .replace("{target_profile_name}", parseResult.profileName)
                .replace("{target_profile_uuid}", parseResult.profileUUID)
                .replace("{profile_uuid}", userData.inGameProfile.uuid)
                .replace("{profile_name}", userData.inGameProfile.username)
        ) {
            dataMap.values.removeIf { it.isExpired() }
            val linkData = LinkData(
                userData.userProfile.uuid,
                userData.service.baseServiceSetting.serviceId,
                parseResult.profileUUID,
                MultiLoginVelocity.instance.config.commandSetting.linkAcceptConfirmAwaitSecond * 1000.toLong() + System.currentTimeMillis(),
                generateLinkCode()
            )
            dataMap[userData.userProfile.uuid] = linkData

            context.source.sendMessage(
                MultiLoginVelocity.instance.message.message("command_execute_link_to_requested")
                    .replace("{service_id}", userData.service.baseServiceSetting.serviceId)
                    .replace("{user_name}", userData.userProfile.username)
                    .replace(
                        "{confirm_second}",
                        MultiLoginVelocity.instance.config.commandSetting.linkAcceptConfirmAwaitSecond
                    )
                    .replace("{verify_code}", linkData.verifyCode)
                    .replace("{target_profile_name}", parseResult.profileName)
                    .replace("{target_profile_uuid}", parseResult.profileName)
            )
        }
    }


    fun generateLinkCode(): String {
        val builder = StringBuilder()
        for (i in 0..5) {
            builder.append((10 * Math.random()).toInt())
        }
        return builder.toString()
    }

    data class LinkData(
        val requesterUUID: UUID,
        val requestServiceId: Int,
        val targetProfileUUID: UUID,
        val expires: Long,
        val verifyCode: String,
    ) {

        fun isExpired(): Boolean {
            return expires < System.currentTimeMillis()
        }
    }
}