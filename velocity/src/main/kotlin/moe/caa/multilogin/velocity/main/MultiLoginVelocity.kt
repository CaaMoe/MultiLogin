package moe.caa.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import moe.caa.multilogin.api.MultiLoginAPI
import moe.caa.multilogin.api.MultiLoginAPIProvider
import moe.caa.multilogin.api.manager.ProfileManager
import moe.caa.multilogin.api.manager.ServiceManager
import org.slf4j.Logger
import java.nio.file.Path


@Plugin(id = "multilogin")
class MultiLoginVelocity @Inject constructor(
    proxyServer: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) : MultiLoginAPI {
    override val profileManager: ProfileManager = TODO()
    override val serviceManager: ServiceManager = TODO()

    init {
        MultiLoginAPIProvider.api = this
    }

    @Subscribe
    fun init(event: ProxyInitializeEvent) {

    }

    @Subscribe
    fun disable(event: ProxyShutdownEvent) {

    }
}