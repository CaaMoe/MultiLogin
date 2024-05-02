package moe.caa.multilogin.core.plugin

import moe.caa.multilogin.api.profile.GameProfile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.*

interface IPlayerManager {
    fun getOnlinePlayers(): List<IPlayerInfo>
    fun getOnlinePlayer(uuid: UUID): IPlayerInfo?
    fun getOnlinePlayer(string: String): IPlayerInfo?
    fun broadcastMessage(component: Component)


    interface IPlayerInfo {
        val audience: Audience
        val inGameProfile: GameProfile

        fun disconnect(component: Component)

        override fun equals(other: Any?): Boolean
        override fun hashCode(): Int
    }
}