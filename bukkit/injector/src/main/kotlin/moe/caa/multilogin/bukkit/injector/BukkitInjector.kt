package moe.caa.multilogin.bukkit.injector

import com.mojang.authlib.minecraft.MinecraftSessionService
import moe.caa.multilogin.api.injector.Injector
import moe.caa.multilogin.api.logger.LoggerProvider
import moe.caa.multilogin.api.main.MultiCoreAPI
import moe.caa.multilogin.bukkit.injector.protocol.PacketHandler
import moe.caa.multilogin.bukkit.injector.proxy.SignatureValidatorInvocationHandler
import moe.caa.multilogin.bukkit.injector.proxy.YggdrasilMinecraftSessionServiceInvocationHandler
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit
import java.lang.reflect.*
import java.util.concurrent.ConcurrentHashMap


class BukkitInjector : Injector {
    companion object {
        val kickMsg: MutableMap<Thread, String> = ConcurrentHashMap()
    }

    override fun inject(api: MultiCoreAPI) {
        var protocolHook = false
        if (api.plugin.runServer.pluginHasEnabled("ProtocolLib")) {
            try {
                PacketHandler().init()
                protocolHook = true
            } catch (e: Throwable) {
                LoggerProvider.getLogger().error("Unable to load ProtocolLib handler, is it up to date?", e)
            }
        }
        if (!protocolHook) {
            LoggerProvider.getLogger().warn(
                "It is strongly recommended that you install ProtocolLib," +
                        " otherwise the client will always prompt 'invalid session' when kicked out by MultiLogin during the login phase."
            )

            LoggerProvider.getLogger().warn(
                "In 1.19.3+ version, MultiLogin will use it to ignore chat session, if this function is not enabled, " +
                        "users who use non-Microsoft authentication will always be kicked out of the game because of 'Invalid signature for profile public key'"
            )
        }
        try {
            // Service 存在，是高版本的！
            val servicesRecordClass = Class.forName("net.minecraft.server.Services")

            val signatureValidatorClass: Class<*>? = try {
                Class.forName("net.minecraft.util.SignatureValidator")
            } catch (ignored: Exception) {
                null
            }

            val pairMinecraftServerAndGetServiceField =
                forceGetNMS((api.plugin as MultiLoginBukkit).server, servicesRecordClass, HashSet())
            val minecraftServer = pairMinecraftServerAndGetServiceField.first
            val services = pairMinecraftServerAndGetServiceField.second[minecraftServer]

            val servicesRecordFields = servicesRecordClass.declaredFields

            class ModifiedPair<A, B>(
                var first: A,
                var second: B
            )

            val constructorArg = arrayListOf<ModifiedPair<Field, Any>>()

            var modified = false
            for (field in servicesRecordFields) {
                if (Modifier.isStatic(field.modifiers)) continue
                field.isAccessible = true
                val anyPair = ModifiedPair(field, field[services])
                constructorArg.add(anyPair)
                if (anyPair.second is MinecraftSessionService) {
                    // 替换MinecraftSessionService
                    anyPair.second = Proxy.newProxyInstance(
                        Thread.currentThread().contextClassLoader,
                        arrayOf(MinecraftSessionService::class.java),
                        YggdrasilMinecraftSessionServiceInvocationHandler(anyPair.second as MinecraftSessionService)
                    )
                    modified = true
                } else if (signatureValidatorClass != null && anyPair.second.javaClass.name.contains("SignatureValidator")) {
                    // 替换SignatureValidator
                    anyPair.second = Proxy.newProxyInstance(
                        Thread.currentThread().contextClassLoader,
                        arrayOf(signatureValidatorClass),
                        SignatureValidatorInvocationHandler(anyPair.second)
                    )
                }
            }

            if (!modified) throw RuntimeException("Unsupported server.")

            val declaredConstructor: Constructor<*> = servicesRecordClass.getDeclaredConstructor(
                *constructorArg.map { it.first.type }.toTypedArray()
            )

            val newServices = declaredConstructor.newInstance(*constructorArg.map { it.second }.toTypedArray())
            pairMinecraftServerAndGetServiceField.second[minecraftServer] = newServices
            return
        } catch (_: java.lang.Exception) {
        }
        val pair = forceGetNMS((api.plugin as MultiLoginBukkit).server, MinecraftSessionService::class.java, HashSet())
        pair.second.isAccessible = true
        pair.second[pair.first] = Proxy.newProxyInstance(
            Thread.currentThread().contextClassLoader,
            arrayOf(MinecraftSessionService::class.java),
            YggdrasilMinecraftSessionServiceInvocationHandler(pair.second[pair.first] as MinecraftSessionService)
        )
    }

    private fun forceGetNMS(source: Any, needGet: Type, ignore: MutableSet<Type>): Pair<Any, Field> {
        var sourceClass: Class<*> = source.javaClass
        // 双重遍历确保能获取到本类和父类所有的Field
        do {
            for (declaredField in sourceClass.declaredFields) {
                try {
                    declaredField.isAccessible = true
                    // 类型匹配，返回Field所在的类的实例和Field
                    if (declaredField.type === needGet) {
                        if (sourceClass.name.startsWith("net.minecraft.")) {
                            return Pair(source, declaredField)
                        }
                    }
                    val o = declaredField[source]
                    if (ignore.add(o.javaClass)) return forceGetNMS(o, needGet, ignore)
                } catch (ignored: Throwable) {
                }
            }
            if (sourceClass.superclass == null) break
        } while (sourceClass.superclass.also { sourceClass = it } != null)
        throw ClassNotFoundException(needGet.typeName)
    }
}