package `fun`.ksnb.multilogin.velocity.command

import com.velocitypowered.api.command.CommandSource
import `fun`.ksnb.multilogin.velocity.main.MultiLoginVelocity
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager

class MultiLoginVelocityCommandHandler(private val plugin: MultiLoginVelocity) {
    private val commandManager =  VelocityCommandManager<Audience>(
        plugin.pluginContainer,
        plugin.server,
        ExecutionCoordinator.coordinatorFor(plugin.multiCore.asyncExecute),
        SenderMapper.create({it}){it as CommandSource}
    )

    fun init(){
        plugin.multiCore.commandHandler.registerCommands(commandManager)
    }
}

