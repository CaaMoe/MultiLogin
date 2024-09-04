package moe.caa.multilogin.velocity.main

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.config.service.BaseService
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.optionals.getOrNull

object InGameData {
    private val inGameDataMap: MutableMap<ConnectedPlayer, InGameEntry> = Collections.synchronizedMap(WeakHashMap())
    private val readyLoginDataMap: MutableMap<MinecraftConnection, ReadyLoginEntry> =
        Collections.synchronizedMap(WeakHashMap())
    private val lock = ReentrantLock()

    private fun getData(connection: ConnectedPlayer) = lock.withLock {
        inGameDataMap[connection]
    }

    fun findByProfileUUID(profileUUID: UUID): InGameEntry? {
        return findByPlayer(MultiLoginVelocity.instance.proxyServer.getPlayer(profileUUID).getOrNull() ?: return null)
    }

    fun findByPlayer(player: Player): InGameEntry? {
        return getData(player as ConnectedPlayer)
    }

    fun findByUser(serviceId: Int, userUUID: UUID): InGameEntry? {
        lock.withLock {
            return inGameDataMap.values.firstOrNull {
                it.userProfile.uuid == userUUID && it.service.baseServiceSetting.serviceId == serviceId
            }
        }
    }

    internal fun putReadyLoginData(connection: MinecraftConnection, entry: ReadyLoginEntry) {
        lock.withLock {
            readyLoginDataMap[connection] = entry
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun login(event: LoginEvent) {
        lock.withLock {
            val ready = readyLoginDataMap.remove((event.player as ConnectedPlayer).connection)
            if (ready == null) {
                event.result = ResultedEvent.ComponentResult.denied(
                    MultiLoginVelocity.instance.message.message("validation_failure_reason_no_use_this_login")
                )
                return
            }
            val transferToInGameEntry = ready.transferToInGameEntry(event.player as ConnectedPlayer)
            inGameDataMap[event.player as ConnectedPlayer] = transferToInGameEntry

            MultiLoginVelocity.instance.logger.info(
                "${transferToInGameEntry.userProfile.username}(uuid: ${
                    transferToInGameEntry.userProfile.uuid
                }) from authentication service ${
                    transferToInGameEntry.service.baseServiceSetting.serviceName
                }(service id: ${
                    transferToInGameEntry.service.baseServiceSetting.serviceId
                }) has been authenticated, profile redirected to ${
                    transferToInGameEntry.inGameProfile.username
                }(uuid: ${
                    transferToInGameEntry.inGameProfile.uuid
                })"
            )
        }
    }

    data class ReadyLoginEntry(
        val connection: MinecraftConnection,
        val userProfile: GameProfile,
        val service: BaseService,
        val inGameProfile: GameProfile,
    ) {
        fun transferToInGameEntry(connectedPlayer: ConnectedPlayer) =
            InGameEntry(connectedPlayer, userProfile, service, inGameProfile)
    }

    data class InGameEntry(
        val connectedPlayer: ConnectedPlayer,
        val userProfile: GameProfile,
        val service: BaseService,
        val inGameProfile: GameProfile,
    )
}