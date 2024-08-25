package moe.caa.multilogin.velocity.command

import com.velocitypowered.api.command.CommandSource
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.componentText
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager

class CommandHandler(
    plugin: MultiLoginVelocity
) {
    private val manager = lazy {
        VelocityCommandManager(
            plugin.proxyServer.pluginManager.getPlugin("multilogin").get(),
            plugin.proxyServer,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity()
        )
    }

    fun init(){
        register { literal("version")
            .permission(COMMAND_VERSION)
            .handler {
                it.sender().sendMessage("泥嚎".componentText())
            }
        }
    }

    private fun register(builder: Builder<CommandSource>.() -> Builder<CommandSource>){
        manager.value.command(builder.invoke(manager.value.commandBuilder("multilogin")))
    }
}