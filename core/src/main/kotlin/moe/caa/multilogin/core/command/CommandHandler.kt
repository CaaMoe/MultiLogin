package moe.caa.multilogin.core.command

import moe.caa.multilogin.core.main.MultiCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.CommandManager


class CommandHandler(val multiCore: MultiCore) {
    companion object{
        const val COMMAND_HEADER = "multilogin"
    }

    fun registerCommands(commandManager: CommandManager<Audience>) {

    }
}