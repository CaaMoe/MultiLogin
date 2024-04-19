package moe.caa.multilogin.core.command

import moe.caa.multilogin.core.command.handles.RELOAD_HANDLER
import moe.caa.multilogin.core.command.handles.VERSION_HANDLER
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.plugin.ICommandSender
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator

class CommandHandler(multiCore: MultiCore) {
    private val manager: CommandManager<ICommandSender> = multiCore.plugin
        .generateCommandManager(ExecutionCoordinator.coordinatorFor(multiCore.plugin.scheduler.executor))

    fun init() {
        registerRootCommands()
    }


    private fun registerRootCommands() {
        // /multiligin version
        register {
            it.literal("version")
                .permission(PERMISSIONS_COMMAND_VERSION)
                .handler(VERSION_HANDLER)
        }

        // /multilogin reload
        register {
            it.literal("reload")
                .permission(PERMISSIONS_COMMAND_RELOAD)
                .handler(RELOAD_HANDLER)
        }
    }

    private fun register(consumer: (builder: Builder<ICommandSender>) -> Builder<ICommandSender>) =
        manager.command(consumer.invoke(manager.commandBuilder("multilogin")))


}