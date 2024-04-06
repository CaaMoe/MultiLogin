package moe.caa.multilogin.core.command

import moe.caa.multilogin.core.command.handles.versionHandle
import moe.caa.multilogin.core.main.MultiCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator

class CommandHandler(multiCore: MultiCore) {
    private val manager: CommandManager<Audience> = multiCore.plugin
        .generateCommandManager(ExecutionCoordinator.coordinatorFor(multiCore.asyncExecute))

    fun init() {
        registerRootCommands()
    }


    private fun registerRootCommands() {
        // /multiligin version
        register { builder ->
            builder.literal("version")
                .permission(PERMISSIONS_COMMAND_VERSION)
                .handler(versionHandle)
        }
    }

    private fun register(consumer: (builder: Builder<Audience>) -> Builder<Audience>) =
        manager.command(consumer.invoke(manager.commandBuilder("multilogin")))


}