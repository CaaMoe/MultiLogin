package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler;

import lombok.Getter;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin;
import net.minecraft.server.network.LoginListener;

/**
 * 接管 LoginListener 的其中一个方法
 */
public class MultiPacketLoginInEncryptionBeginHandler {
    @Getter
    private final LoginListener loginListener;
    private final MultiCoreAPI multiCoreAPI;

    public MultiPacketLoginInEncryptionBeginHandler(LoginListener loginListener, MultiCoreAPI multiCoreAPI) {
        this.loginListener = loginListener;
        this.multiCoreAPI = multiCoreAPI;
    }


    public void handle(PacketLoginInEncryptionBegin packetLoginInEncryptionBegin){
        loginListener.disconnect("Unsupported!");
    }
}
