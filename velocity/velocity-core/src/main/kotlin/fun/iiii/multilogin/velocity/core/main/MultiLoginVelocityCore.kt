package `fun`.iiii.multilogin.velocity.core.main

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import com.velocitypowered.proxy.config.VelocityConfiguration
import `fun`.iiii.multilogin.velocity.bootstrap.MultiLoginVelocityBootstrap
import `fun`.iiii.multilogin.velocity.core.inject.VelocityInjector
import `fun`.iiii.multilogin.velocity.core.listener.Listener
import `fun`.iiii.multilogin.velocity.core.manager.VelocityPlayerManager
import moe.caa.multilogin.api.schedule.IScheduler
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.plugin.ExtendedPlatform
import moe.caa.multilogin.core.plugin.IPlayerManager
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.io.File

class MultiLoginVelocityCore(
    override val bootstrap: MultiLoginVelocityBootstrap,
) : ExtendedPlatform {
    override val dataFolder: File = bootstrap.dataFolder
    override val tempFolder: File = bootstrap.tempFolder
    override val scheduler: IScheduler = bootstrap.scheduler
    override val onlineMode: Boolean = bootstrap.proxyServer.configuration.isOnlineMode
    override val profileForwarding: Boolean =
        (bootstrap.proxyServer.configuration as VelocityConfiguration).playerInfoForwardingMode != PlayerInfoForwarding.NONE
    override val consoleCommandSender: Audience = bootstrap.proxyServer.consoleCommandSource
    override val playerManager: IPlayerManager = VelocityPlayerManager(this)

    private val listener = Listener(this)

    val multiCore = MultiCore(this)

    override fun enable() {
        multiCore.enable()
        bootstrap.proxyServer.eventManager.register(bootstrap, listener)
        VelocityInjector(this).inject()
    }

    override fun disable() {
        multiCore.disable()
    }

    override fun generateCommandManager(executionCoordinator: ExecutionCoordinator<Audience>) =
        VelocityCommandManager(
            bootstrap.proxyServer.pluginManager.ensurePluginContainer(bootstrap),
            bootstrap.proxyServer,
            executionCoordinator,
            SenderMapper.create({ it }, { it as CommandSource })
        )
}