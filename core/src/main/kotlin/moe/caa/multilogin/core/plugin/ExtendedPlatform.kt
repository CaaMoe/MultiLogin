package moe.caa.multilogin.core.plugin

import moe.caa.multilogin.api.schedule.IScheduler
import moe.caa.multilogin.loader.api.ExtendedService
import moe.caa.multilogin.loader.api.IBootstrap
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import java.io.File

interface ExtendedPlatform : ExtendedService {
    val bootstrap: IBootstrap
    val dataFolder: File
    val tempFolder: File
    val scheduler: IScheduler

    val onlineMode: Boolean
    val profileForwarding: Boolean
    val consoleCommandSender: Audience
    val playerManager: IPlayerManager<*>

    fun generateCommandManager(executionCoordinator: ExecutionCoordinator<Audience>): CommandManager<Audience>
}