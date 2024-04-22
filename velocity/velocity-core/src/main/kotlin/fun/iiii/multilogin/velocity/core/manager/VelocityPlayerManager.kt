package `fun`.iiii.multilogin.velocity.core.manager

import com.velocitypowered.api.proxy.Player
import `fun`.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore
import `fun`.iiii.multilogin.velocity.core.util.toMultiLoginGameProfile
import moe.caa.multilogin.core.plugin.IPlayerManager
import net.kyori.adventure.text.Component
import java.util.*

class VelocityPlayerManager(private val core: MultiLoginVelocityCore) : IPlayerManager {
    override fun getOnlinePlayers() = core.bootstrap.proxyServer.allPlayers.map { VelocityPlayerInfo(it) }
    override fun getOnlinePlayer(uuid: UUID): VelocityPlayerInfo? =
        core.bootstrap.proxyServer.getPlayer(uuid).map { VelocityPlayerInfo(it) }.orElse(null)

    override fun getOnlinePlayer(string: String): IPlayerManager.IPlayerInfo? =
        core.bootstrap.proxyServer.getPlayer(string).map { VelocityPlayerInfo(it) }.orElse(null)

    class VelocityPlayerInfo(private val player: Player) : IPlayerManager.IPlayerInfo {
        override val audience = player
        override val gameProfile = player.gameProfile.toMultiLoginGameProfile()

        override fun kick(component: Component) {
            player.disconnect(component)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as VelocityPlayerInfo
            return player == other.player
        }

        override fun hashCode(): Int {
            return player.hashCode()
        }
    }
}