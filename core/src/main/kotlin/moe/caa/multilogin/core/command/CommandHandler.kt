package moe.caa.multilogin.core.command

import moe.caa.multilogin.core.command.handles.RELOAD_HANDLER
import moe.caa.multilogin.core.command.parser.ServiceIdArgumentParser
import moe.caa.multilogin.core.main.MultiCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.Command.Builder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.parser.standard.IntegerParser

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
            literal("test")
                .required("grdsfrgsd", ServiceIdArgumentParser.serviceIdParser())
                .required("asdasdasd", IntegerParser.integerParser())
                .required("dsgdrhfthdf", IntegerParser.integerParser())
                .required("yujyhtdgrsfdd", IntegerParser.integerParser())
                .required("iumgfgdgvdgfn", IntegerParser.integerParser())
                .handler {

                }
        }
    }

    private fun register(builder: Builder<Audience>.() -> Builder<Audience>){
        manager.command(builder.invoke(manager.commandBuilder("multilogin")))
    }

}