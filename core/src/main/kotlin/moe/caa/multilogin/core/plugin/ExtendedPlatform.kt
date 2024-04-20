package moe.caa.multilogin.core.plugin

import moe.caa.multilogin.api.schedule.IScheduler
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import java.io.File

interface ExtendedPlatform {
    val dataFolder: File
    val tempFolder: File
    val scheduler: IScheduler

    val onlineMode: Boolean
    val profileForwarding: Boolean
    val consoleCommandSender: Audience

    fun generateCommandManager(executionCoordinator: ExecutionCoordinator<Audience>): CommandManager<Audience>
}