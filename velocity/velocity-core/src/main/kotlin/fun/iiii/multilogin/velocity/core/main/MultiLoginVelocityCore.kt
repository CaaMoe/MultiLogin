package `fun`.iiii.multilogin.velocity.core.main

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import com.velocitypowered.proxy.config.VelocityConfiguration
import `fun`.iiii.multilogin.velocity.bootstrap.MultiLoginVelocityBootstrap
import moe.caa.multilogin.api.schedule.IScheduler
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.plugin.ExtendedPlatform
import moe.caa.multilogin.loader.api.IPlatformCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.io.File

class MultiLoginVelocityCore(
    private val bootstrap: MultiLoginVelocityBootstrap,
) : IPlatformCore<MultiLoginVelocityBootstrap>, ExtendedPlatform {
    override val dataFolder: File = bootstrap.dataFolder
    override val tempFolder: File = bootstrap.tempFolder
    override val scheduler: IScheduler = bootstrap.scheduler
    override val onlineMode: Boolean = bootstrap.proxyServer.configuration.isOnlineMode
    override val profileForwarding: Boolean =
        (bootstrap.proxyServer.configuration as VelocityConfiguration).playerInfoForwardingMode != PlayerInfoForwarding.NONE
    override val consoleCommandSender: Audience = bootstrap.proxyServer.consoleCommandSource

    val multiCore = MultiCore(this)

    override fun enable() {
        multiCore.enable()
    }

    override fun disable() {
        multiCore.disable()
    }

    override fun getBootstrap() = bootstrap
    override fun generateCommandManager(executionCoordinator: ExecutionCoordinator<Audience>) =
        VelocityCommandManager(
            bootstrap.proxyServer.pluginManager.ensurePluginContainer(bootstrap),
            bootstrap.proxyServer,
            executionCoordinator,
            SenderMapper.create({ it }, { it as CommandSource })
        )
}