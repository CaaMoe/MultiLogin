package moe.caa.multilogin.core.command

import moe.caa.multilogin.core.command.handles.LIST_HANDLER
import moe.caa.multilogin.core.command.handles.RELOAD_HANDLER
import moe.caa.multilogin.core.main.MultiCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator

class CommandHandler(multiCore: MultiCore) {
    private val manager: CommandManager<Audience> = multiCore.plugin
        .generateCommandManager(ExecutionCoordinator.coordinatorFor(multiCore.plugin.scheduler.executor))

    fun init() {
        registerRootCommands()
    }


    private fun registerRootCommands() {
        register {
            literal("reload")
                .permission(PERMISSIONS_COMMAND_RELOAD)
                .handler(RELOAD_HANDLER)
        }

        register {
            literal("list")
                .permission(PERMISSIONS_COMMAND_LIST)
                .handler(LIST_HANDLER)
        }
    }

    private fun register(builder: Builder<Audience>.() -> Builder<Audience>){
        manager.command(builder.invoke(manager.commandBuilder("multilogin")))
    }

}