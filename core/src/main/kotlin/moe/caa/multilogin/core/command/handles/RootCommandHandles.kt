package moe.caa.multilogin.core.command.handles

import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.builddata.getBuildData
import moe.caa.multilogin.core.resource.message.language
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.incendo.cloud.execution.CommandExecutionHandler

val VERSION_HANDLER = CommandExecutionHandler<Audience> {
    it.sender().sendMessage(Component.text("Version: ${getBuildData("version")}"))
    it.sender().sendMessage(Component.text("Build Type: ${getBuildData("build_type")}"))
    it.sender().sendMessage(Component.text("Build By: ${getBuildData("build_by")}"))
    it.sender().sendMessage(Component.text("Build Time: ${getBuildData("build_timestamp")}"))
    it.sender().sendMessage(Component.text("Build Revision: ${getBuildData("build_revision")}"))
}

val RELOAD_HANDLER = CommandExecutionHandler<Audience> {
    val sender = it.sender()
    runCatching {
        MultiCore.instance.reload()
    }.onFailure {
        sender.sendMessage(language("command_reload_exception"))
    }.onSuccess {
        sender.sendMessage(language("command_reloaded"))
    }
}