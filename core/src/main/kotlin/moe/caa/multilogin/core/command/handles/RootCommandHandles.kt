package moe.caa.multilogin.core.command.handles

import moe.caa.multilogin.api.exception.BreakSignalException
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.manager.DataManager
import moe.caa.multilogin.core.plugin.IPlayerManager
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import moe.caa.multilogin.core.resource.message.language
import moe.caa.multilogin.core.resource.message.replace
import moe.caa.multilogin.core.util.logError
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.incendo.cloud.execution.CommandExecutionHandler

val LIST_HANDLER = CommandExecutionHandler<Audience> { context ->
    val collect: MutableMap<BaseService, MutableList<DataManager.VerifiedData>> = HashMap()
    val unidentified: MutableList<IPlayerManager.IPlayerInfo> = ArrayList()

    MultiCore.instance.api.services.forEach { collect[it] = java.util.ArrayList() }
    MultiCore.instance.plugin.playerManager.getOnlinePlayers().forEach {
        val data = MultiCore.instance.api.getPlayerData(it.inGameProfile.uuid)
        if (data == null) {
            unidentified.add(it)
        } else {
            collect.computeIfAbsent(data.authService) { ArrayList() }.add(data)
        }
    }

    var allList = Component.empty()
    var firstElementService = true

    collect.forEach { it ->
        var currentList = Component.empty()
        var firstElementPlayer = true
        it.value.forEach {
            if (firstElementPlayer) {
                firstElementPlayer = false
            } else {
                currentList = currentList.append {
                    language("command_list_player_entry_delimiter")
                }
            }
            currentList = currentList.append {
                language("command_list_player_entry")
                    .replace("%profile_username%", it.inGameProfile.username)
                    .replace("%login_username%", it.loginProfile.username)
            }
        }

        if (firstElementService) {
            firstElementService = false
        } else {
            allList = allList.append {
                language("command_list_service_entry_delimiter")
            }
        }
        allList = allList.append {
            language("command_list_service_entry")
                .replace("%service_name%", it.key.serviceName)
                .replace("%service_id%", it.key.serviceId)
                .replace("%current_count%", it.value.size)
                .replace("%current_list%", currentList)
        }
    }

    context.sender().sendMessage {
        language("command_list_all")
            .replace("%online_count%", MultiCore.instance.plugin.playerManager.getOnlinePlayers().size)
            .replace("%all_list%", allList)
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
