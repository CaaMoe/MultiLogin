package moe.caa.multilogin.bukkit.injector.proxy

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.properties.Property
import moe.caa.multilogin.api.internal.auth.AuthResult
import moe.caa.multilogin.api.internal.logger.LoggerProvider
import moe.caa.multilogin.api.internal.skinrestorer.SkinRestorerResult
import moe.caa.multilogin.bukkit.injector.BukkitInjector
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit
import moe.caa.multilogin.core.auth.LoginAuthResult
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.net.InetSocketAddress
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class YggdrasilMinecraftSessionServiceInvocationHandler(
    private val vanillaSessionService: MinecraftSessionService
) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        if (method.name.equals("hasJoinedServer")) {
            val profileName: String = if(args[0] is String) args[0] as String else (args[0] as GameProfile).name
            val serverId: String = args[1] as String
            val ip = if (args.size == 3 && args[2] is InetSocketAddress) URLEncoder.encode(
                (args[2] as InetSocketAddress).address.hostAddress,
                StandardCharsets.UTF_8
            ) else ""
            return handle(method, profileName, serverId, ip)
        }
        return method.invoke(vanillaSessionService, *args)
    }

    private fun handle(method: Method, profileName: String, serverId: String, ip: String): Any? {
        val multiCoreAPI = MultiLoginBukkit.getInstance().multiCoreAPI
        try {
            val result = multiCoreAPI.authHandler.auth(profileName, serverId, ip) as LoginAuthResult
            if (result.result == AuthResult.Result.ALLOW) {
                var gameProfile: moe.caa.multilogin.api.profile.GameProfile = result.response
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
                return generateResponse(method.returnType, gameProfile)
            } else {
                BukkitInjector.kickMsg[Thread.currentThread()] = result.kickMessage
                LoggerProvider.getLogger().info("$profileName was kicked out for ${result.kickMessage}")
                return null
            }
        } catch (e: Throwable) {
            val message = multiCoreAPI.languageHandler.getMessage("auth_error")
            BukkitInjector.kickMsg[Thread.currentThread()] = message
            LoggerProvider.getLogger().info("$profileName was kicked out for $message")
            LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e)
        }
        return null
    }

    private fun generateResponse(returnType: Type, response: moe.caa.multilogin.api.profile.GameProfile): Any {
        val result = GameProfile(response.id, response.name)
        response.propertyMap.forEach { (k, u) ->
            result.properties.put(k, Property(u.name, u.value, u.signature))
        }
        if(returnType == result.javaClass){
            return result
        }

        return Class.forName("com.mojang.authlib.yggdrasil.ProfileResult")
            .getConstructor(Class.forName("com.mojang.authlib.GameProfile"))
            .newInstance(result)
    }
}