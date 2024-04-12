package moe.caa.multilogin.core.auth.service.yggdrasil

import moe.caa.multilogin.core.auth.AuthHandler
import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilService

class YggdrasilAuthenticator(
    val authHandler: AuthHandler
) {

    fun hasJoined(username: String, serverId: String, playerIp: String?): YggdrasilAuthenticationResult {
        val services = GeneralConfiguration.services.map { it.value }.filterIsInstance<YggdrasilService>()
        if (services.isEmpty()) {
            return YggdrasilAuthenticationResult.ofNoYggdrasilService()
        }
        TODO()
    }
}