package moe.caa.multilogin.bukkit.injector.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.injector.GamePhase
import com.comphenix.protocol.wrappers.WrappedChatComponent
import moe.caa.multilogin.bukkit.injector.BukkitInjector
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit

class PacketHandler {
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
            .gamePhase(GamePhase.PLAYING)
            .clientSide()
            .plugin(MultiLoginBukkit.getInstance())
            .types(PacketType.Play.Client.CHAT_SESSION_UPDATE)){
        override fun onPacketReceiving(event: PacketEvent) {
            event.isReadOnly = false
            event.isCancelled = true
        }
    }
}