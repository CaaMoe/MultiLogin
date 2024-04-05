package `fun`.iiii.multilogin.velocity.command

import com.velocitypowered.api.command.CommandSource
import `fun`.iiii.multilogin.velocity.main.MultiLoginVelocity
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager

class MultiLoginVelocityCommandHandler(private val plugin: MultiLoginVelocity) {
    private lateinit var commandManager: VelocityCommandManager<Audience>

    fun init() {
        commandManager = VelocityCommandManager<Audience>(
            plugin.pluginContainer,
            plugin.server,
            ExecutionCoordinator.coordinatorFor(plugin.multiCore.asyncExecute),
            SenderMapper.create({ it }) { it as CommandSource }
        )
        plugin.multiCore.commandHandler.registerCommands(commandManager)
    }
}

