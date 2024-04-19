package moe.caa.multilogin.core.plugin

import moe.caa.multilogin.api.plugin.IScheduler
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import java.io.File

interface ExtendedPlatform {
    val dataFolder: File
    val tempFolder: File
    val scheduler: IScheduler

    val onlineMode: Boolean
    val profileForwarding: Boolean
    val consoleCommandSender: ICommandSender

    fun generateCommandManager(executionCoordinator: ExecutionCoordinator<ICommandSender>): CommandManager<ICommandSender>
}