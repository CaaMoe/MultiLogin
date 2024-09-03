package moe.caa.multilogin.velocity.command

import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.command.sub.WhitelistCommand
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.componentText
import moe.caa.multilogin.velocity.util.getResource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.exception.NoPermissionException
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.io.InputStreamReader
import java.util.*

class CommandHandler(
    val plugin: MultiLoginVelocity
) {
    private val manager = lazy {
        VelocityCommandManager(
            plugin.proxyServer.pluginManager.getPlugin("multilogin").get(),
            plugin.proxyServer,
            ExecutionCoordinator.asyncCoordinator(),
            SenderMapper.identity()
        )
    }

    fun init(){
        manager.value.exceptionController().apply {
            clearHandlers()
            registerHandler(Throwable::class.java) {
                it.context().sender().sendMessage(plugin.message.message("command_execute_failed_error"))
                plugin.logger.error(
                    "An exception was thrown while executing instruction ${it.context().rawInput()}.",
                    it.exception()
                )
            }
            registerHandler(NoPermissionException::class.java) {
                it.context().sender().sendMessage(
                    plugin.message.message("command_execute_failed_no_permission")
                        .replace("{permission}", it.exception().permissionResult().permission().permissionString())
                )
            }
            registerHandler(CommandParseException::class.java) {
                it.context().sender().sendMessage(it.exception().msg)
            }
        }

        registerAboutCommand()
        registerReloadCommand()
        WhitelistCommand(this).register()
    }

    private fun registerReloadCommand() = register {
        literal("reload")
            .permission(COMMAND_RELOAD)
            .handler { context ->
                plugin.reload()
                context.sender().sendMessage(plugin.message.message("command_execute_reload_done"))
            }
    }

    private fun registerAboutCommand() = register {
        literal("about")
            .permission(COMMAND_ABOUT)
            .handler { context ->
                getResource("builddata").use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        Properties().apply {
                            load(reader)

                            context.sender().sendMessage("MultiLogin v${this["Plugin-Version"]}".componentText())
                            context.sender()
                                .sendMessage("Build Datetime: ${this["Build-Datetime"]}".componentText())
                            context.sender()
                                .sendMessage("Build Revision: ${this["Build-Revision"]}".componentText())
                            context.sender().sendMessage(
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


    fun register(builders: Builder<CommandSource>.() -> Builder<CommandSource>) {
        manager.value.command(builders.invoke(manager.value.commandBuilder("multilogin")))
    }

    class CommandParseException(val msg: Component) : IllegalArgumentException()
}