package moe.caa.bukkit.multilogin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import moe.caa.bukkit.multilogin.MultiLogin;


public class PacketListener extends PacketAdapter {

    private PacketListener(MultiLogin plugin) {
        super(params().plugin(plugin)
                .types(PacketType.Login.Server.DISCONNECT).
                        optionAsync()
        );
    }

    public static void register(MultiLogin plugin){

    }

    @Override
    public void onPacketSending(PacketEvent event) {
        super.onPacketSending(event);
    }
}
