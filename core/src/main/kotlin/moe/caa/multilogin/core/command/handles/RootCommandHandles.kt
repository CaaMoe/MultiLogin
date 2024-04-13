package moe.caa.multilogin.core.command.handles

import moe.caa.multilogin.api.exception.BreakSignalException
import moe.caa.multilogin.api.logger.logError
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.builddata.getBuildData
import moe.caa.multilogin.core.resource.message.language
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.incendo.cloud.execution.CommandExecutionHandler

val VERSION_HANDLER = CommandExecutionHandler<Audience> {
    it.sender().sendMessage(Component.text("Build Version: ${getBuildData("version")}"))
    it.sender().sendMessage(Component.text("Build Type: ${getBuildData("build_type")}"))
    it.sender().sendMessage(Component.text("Build By: ${getBuildData("build_by")}"))
    it.sender().sendMessage(Component.text("Build Time: ${getBuildData("build_timestamp")}"))
    it.sender().sendMessage(Component.text("Build Revision: ${getBuildData("build_revision")}"))
    it.sender().sendMessage {
        Component.text("Source code: ").append {
            Component.text("https://github.com/CaaMoe/MultiLogin")
                .clickEvent(ClickEvent.openUrl("https://github.com/CaaMoe/MultiLogin"))
        }
    }
}

val RELOAD_HANDLER = CommandExecutionHandler<Audience> { context ->
    runCatching {
        MultiCore.instance.reload()
    }.onFailure { throwable ->
        if (throwable is BreakSignalException) {
            throwable.message?.let { logError(it) }
        } else {
            logError("Exception encountered while reloading.", throwable)
        }
        context.sender().sendMessage(language("command_reload_exception"))
    }.onSuccess {
        context.sender().sendMessage(language("command_reloaded"))
    }
}