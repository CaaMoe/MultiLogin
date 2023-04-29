package moe.caa.multilogin.bukkit.injector.proxy

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.properties.Property
import moe.caa.multilogin.api.auth.AuthResult
import moe.caa.multilogin.api.logger.LoggerProvider
import moe.caa.multilogin.api.skinrestorer.SkinRestorerResult
import moe.caa.multilogin.bukkit.injector.BukkitInjector
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit
import moe.caa.multilogin.core.auth.LoginAuthResult
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class YggdrasilMinecraftSessionServiceInvocationHandler(
    private val vanillaSessionService: MinecraftSessionService
) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        if (method.name.equals("hasJoinedServer")) {
            val profile: GameProfile = args[0] as GameProfile
            val serverId: String = args[1] as String
            val ip = if (args.size == 3 && args[2] is InetSocketAddress) URLEncoder.encode(
                (args[2] as InetSocketAddress).address.hostAddress,
                StandardCharsets.UTF_8
            ) else ""
            return handle(profile, serverId, ip)
        }
        return method.invoke(vanillaSessionService, args)
    }

    private fun handle(profile: GameProfile, serverId: String, ip: String): GameProfile? {
        val multiCoreAPI = MultiLoginBukkit.getInstance().multiCoreAPI
        try {
            val result = multiCoreAPI.authHandler.auth(profile.name, serverId, ip) as LoginAuthResult
            if (result.result == AuthResult.Result.ALLOW) {
                var gameProfile: moe.caa.multilogin.api.auth.GameProfile = result.response
                try {
                    val restorerResult: SkinRestorerResult = multiCoreAPI.skinRestorerHandler.doRestorer(result)
                    if (restorerResult.throwable != null) {
                        LoggerProvider.getLogger()
                            .error("An exception occurred while processing the skin repair.", restorerResult.throwable)
                    }
                    LoggerProvider.getLogger().debug(
                        String.format(
                            "Skin restore result of %s is %s.",
                            result.baseServiceAuthenticationResult.response.name,
                            restorerResult.reason
                        )
                    )
                    if (restorerResult.response != null) {
                        gameProfile = restorerResult.response
                    }
                } catch (e: Exception) {
                    LoggerProvider.getLogger().debug(
                        String.format(
                            "Skin restore result of %s is %s.",
                            result.baseServiceAuthenticationResult.response.name,
                            "error"
                        )
                    )
                    LoggerProvider.getLogger().debug("An exception occurred while processing the skin repair.", e)
                }
                return generateGameProfile(gameProfile)
            } else {
                BukkitInjector.kickMsg[Thread.currentThread()] = result.kickMessage
                LoggerProvider.getLogger().info("${profile.name} was kicked out for ${result.kickMessage}")
                return null
            }
        } catch (e: Throwable) {
            val message = multiCoreAPI.languageHandler.getMessage("auth_error")
            BukkitInjector.kickMsg[Thread.currentThread()] = message
            LoggerProvider.getLogger().info("${profile.name} was kicked out for $message")
            LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e)
        }
        return null
    }

    private fun generateGameProfile(response: moe.caa.multilogin.api.auth.GameProfile): GameProfile {
        val result = GameProfile(response.id, response.name)
        response.propertyMap.forEach { (k, u) ->
            result.properties.put(k, Property(u.name, u.value, u.signature))
        }
        return result
    }
}