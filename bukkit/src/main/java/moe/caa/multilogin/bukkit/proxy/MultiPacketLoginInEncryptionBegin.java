package moe.caa.multilogin.bukkit.proxy;

import moe.caa.multilogin.bukkit.impl.BukkitUserLogin;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.PacketLoginInEncryptionBegin;
import net.minecraft.server.v1_16_R3.PacketLoginInListener;

public class MultiPacketLoginInEncryptionBegin extends PacketLoginInEncryptionBegin {

    public void a(PacketLoginInListener var0) {
        //var0.a(this);

        // 服务端加密妹有完成，消息不能完整送达
        ((LoginListener) var0).disconnect("爬开");
        BukkitUserLogin login = new BukkitUserLogin((LoginListener) var0);
    }
}
