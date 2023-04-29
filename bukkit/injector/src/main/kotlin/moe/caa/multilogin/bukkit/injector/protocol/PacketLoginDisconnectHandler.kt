package moe.caa.multilogin.bukkit.injector.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import moe.caa.multilogin.bukkit.injector.BukkitInjector
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit

class PacketLoginDisconnectHandler {
    fun init() {
        val manager = ProtocolLibrary.getProtocolManager()
        val adapterParameters: PacketAdapter.AdapterParameteters = PacketAdapter.params()
            .loginPhase()
            .serverSide()
            .plugin(MultiLoginBukkit.getInstance())
            .types(PacketType.Login.Server.DISCONNECT)

        manager.addPacketListener(InnerHandler(adapterParameters))
    }

    private class InnerHandler(adapterParameters: AdapterParameteters) : PacketAdapter(adapterParameters) {
        override fun onPacketSending(event: PacketEvent) {
            val s = BukkitInjector.kickMsg.remove(Thread.currentThread()) ?: return
            val packet = event.packet
            event.isReadOnly = false
            packet.chatComponents.write(0, WrappedChatComponent.fromText(s))
            event.isReadOnly = true
        }
    }
}