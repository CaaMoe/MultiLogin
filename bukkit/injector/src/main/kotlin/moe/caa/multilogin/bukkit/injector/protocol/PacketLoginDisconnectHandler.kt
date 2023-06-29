package moe.caa.multilogin.bukkit.injector.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import moe.caa.multilogin.api.logger.LoggerProvider
import moe.caa.multilogin.bukkit.injector.BukkitInjector
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit

class PacketLoginDisconnectHandler {
    fun init() {
        val manager = ProtocolLibrary.getProtocolManager()

        manager.addPacketListener(DisconnectHandler())
        manager.addPacketListener(PlayerSessionHandler())
    }

    private class DisconnectHandler : PacketAdapter(
        params()
        .loginPhase()
        .serverSide()
        .plugin(MultiLoginBukkit.getInstance())
        .types(PacketType.Login.Server.DISCONNECT)) {
        override fun onPacketSending(event: PacketEvent) {
            val s = BukkitInjector.kickMsg.remove(Thread.currentThread()) ?: return
            val packet = event.packet
            event.isReadOnly = false
            packet.chatComponents.write(0, WrappedChatComponent.fromText(s))
            event.isReadOnly = true
        }
    }

    private class PlayerSessionHandler() : PacketAdapter(
        params()
            .loginPhase()
            .serverSide()
            .plugin(MultiLoginBukkit.getInstance())
            .types(PacketType.Play.Client.CHAT_SESSION_UPDATE)){
        override fun onPacketSending(event: PacketEvent) {
            event.isReadOnly = false
            event.isCancelled = true
            LoggerProvider.getLogger().debug("Player session ignored: ${event.packet.uuiDs.values[0]}")
        }
    }
}