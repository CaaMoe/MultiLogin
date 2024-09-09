package moe.caa.multilogin.velocity.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import moe.caa.multilogin.velocity.auth.GameData
import moe.caa.multilogin.velocity.auth.OfflineGameData
import moe.caa.multilogin.velocity.auth.OnlineGameData
import moe.caa.multilogin.velocity.command.parser.PlayerParser
import moe.caa.multilogin.velocity.command.sub.LinkCommand
import moe.caa.multilogin.velocity.command.sub.ProfileCommand
import moe.caa.multilogin.velocity.command.sub.UserCommand
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import java.io.InputStreamReader
import java.util.*

class CommandHandler(
    val plugin: MultiLoginVelocity
) {
    private lateinit var command: LiteralArgumentBuilder<CommandSource>

    private val confirmCommandMap = WeakHashMap<CommandSource, ConfirmStorage>()

    fun init(){
        command = BrigadierCommand.literalArgumentBuilder("multilogin")
        command.permission(COMMAND_BASE)

        registerAboutCommand()
        registerReloadCommand()
        registerConfirmCommand()
        registerInfoCommand()
        registerListCommand()
        LinkCommand(this).register()
        UserCommand(this).register()
        ProfileCommand(this).register()

        plugin.proxyServer.commandManager.register(BrigadierCommand(command))
    }

    private fun registerListCommand() = register {
        thenLiteral("list") {
            permission(COMMAND_LIST)
            handler {
                TODO()
            }
        }
    }

    private fun registerReloadCommand() = register {
        thenLiteral("reload") {
            permission(COMMAND_RELOAD)
            handler {
                plugin.reload()
                source.sendMessage(plugin.message.message("command_execute_reload_done"))
            }
        }
    }


    private fun registerInfoCommand() = register {
        thenLiteral("me") {
            permission(COMMAND_ME)
            handler {
                handleInfoCommand(source, true, player.gameProfile, player.gameData)

            }
        }
        thenLiteral("info") {
            permission(COMMAND_INFO)
            thenArgument("target", PlayerParser) {
                handler {
                    val player = getArgument("target", Player::class.java)

                    handleInfoCommand(source, player == source, player.gameProfile, player.gameData)
                }
            }
        }
    }


    private fun registerAboutCommand() = register {
        thenLiteral("about") {
            permission(COMMAND_ABOUT)
            handler {
                getResource("builddata").use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        Properties().apply {
                            load(reader)

                            source.sendMessage("MultiLogin v${this["Plugin-Version"]}".componentText())
                            source.sendMessage("Build Datetime: ${this["Build-Datetime"]}".componentText())
                            source.sendMessage("Build Revision: ${this["Build-Revision"]}".componentText())
                            source.sendMessage(
                                "Source Code: ".componentText().append(
                                    "[Github]".componentText().style(
                                        Style.style(TextDecoration.UNDERLINED)
                                    ).clickEvent(ClickEvent.openUrl("https://github.com/CaaMoe/MultiLogin"))
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    fun register(builders: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
        builders.invoke(command)
    }

    private fun handleInfoCommand(
        sender: CommandSource,
        self: Boolean,
        inGameProfile: com.velocitypowered.api.util.GameProfile,
        data: GameData?
    ) {
        if (data == null || (data is OfflineGameData && !data.verified)) {
            if (self) {
                sender.sendMessage(
                    plugin.message.message("command_execute_info_self_offline_no_verified")
                        .replace("{profile_uuid}", inGameProfile.id)
                        .replace("{profile_name}", inGameProfile.name)
                )
            } else {
                sender.sendMessage(
                    plugin.message.message("command_execute_info_target_offline_no_verified")
                        .replace("{profile_uuid}", inGameProfile.id)
                        .replace("{profile_name}", inGameProfile.name)
                )
            }
            return
        }
        when (data) {
            is OfflineGameData -> {
                if (self) {
                    sender.sendMessage(
                        plugin.message.message("command_execute_info_self_offline")
                            .replace("{profile_uuid}", inGameProfile.id)
                            .replace("{profile_name}", inGameProfile.name)
                    )
                } else {
                    sender.sendMessage(
                        plugin.message.message("command_execute_info_target_offline")
                            .replace("{profile_uuid}", inGameProfile.id)
                            .replace("{profile_name}", inGameProfile.name)
                    )
                }
            }

            is OnlineGameData -> {
                if (self) {
                    sender.sendMessage(
                        plugin.message.message("command_execute_info_self_online")
                            .replace("{profile_uuid}", data.inGameProfile.uuid)
                            .replace("{profile_name}", data.inGameProfile.username)
                            .replace("{service_id}", data.service.baseServiceSetting.serviceId)
                            .replace("{service_name}", data.service.baseServiceSetting.serviceName)
                            .replace("{user_name}", data.userProfile.username)
                            .replace("{user_uuid}", data.userProfile.uuid)
                    )
                } else {
                    sender.sendMessage(
                        plugin.message.message("command_execute_info_target_online")
                            .replace("{profile_uuid}", data.inGameProfile.uuid)
                            .replace("{profile_name}", data.inGameProfile.username)
                            .replace("{service_id}", data.service.baseServiceSetting.serviceId)
                            .replace("{service_name}", data.service.baseServiceSetting.serviceName)
                            .replace("{user_name}", data.userProfile.username)
                            .replace("{user_uuid}", data.userProfile.uuid)
                    )
                }
            }
        }
    }


    private fun registerConfirmCommand() = register {
        thenLiteral("confirm") {
            permission(COMMAND_CONFIRM)
            handler {
                synchronized(confirmCommandMap) {
                    val data = confirmCommandMap.remove(source)
                    if (data == null || data.expires < System.currentTimeMillis()) {
                        source.sendMessage(plugin.message.message("command_execute_confirm_nothing"))
                        return@handler
                    }
                    data.handle.invoke()
                }
            }
        }
    }


    fun needConfirm(
        source: CommandSource,
        aim: Component,
        confirmSecond: Int = MultiLoginVelocity.instance.config.commandSetting.confirmAwaitSecond,
        handle: () -> Unit
    ) {
        synchronized(confirmCommandMap) {
            confirmCommandMap[source] = ConfirmStorage(System.currentTimeMillis() + confirmSecond * 1000, handle)
        }

        source.sendMessage(
            plugin.message.message("command_execute_confirm_warn_tip")
                .replace("{aim}", aim)
                .replace("{confirm_second}", confirmSecond)
        )
    }

    data class ConfirmStorage(
        val expires: Long,
        val handle: () -> Unit
    )
}