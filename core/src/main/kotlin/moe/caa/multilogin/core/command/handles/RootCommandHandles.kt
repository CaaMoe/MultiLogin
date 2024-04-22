package moe.caa.multilogin.core.command.handles

import moe.caa.multilogin.api.exception.BreakSignalException
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.message.language
import moe.caa.multilogin.core.util.logError
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.execution.CommandExecutionHandler

val LIST = CommandExecutionHandler<Audience> { context ->

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