package moe.caa.multilogin.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;

public class ProtocolListener extends PacketAdapter {

    public ProtocolListener(MultiLoginBukkit plugin) {
        super(params().plugin(plugin).optionAsync().types(PacketType.Login.Client.ENCRYPTION_BEGIN, PacketType.Login.Client.START));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        event.getPacket();
    }
}
