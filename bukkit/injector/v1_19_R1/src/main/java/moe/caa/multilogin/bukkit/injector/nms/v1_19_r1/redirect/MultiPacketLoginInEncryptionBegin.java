package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.redirect;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler.MultiPacketLoginInEncryptionBeginHandler;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin;
import net.minecraft.network.protocol.login.PacketLoginInListener;
import net.minecraft.server.network.LoginListener;

/**
 * PacketLoginInEncryptionBegin 包处理拦截器
 */
public class MultiPacketLoginInEncryptionBegin extends PacketLoginInEncryptionBegin {
    private final MultiCoreAPI multiCoreAPI;

    public MultiPacketLoginInEncryptionBegin(PacketDataSerializer var0, MultiCoreAPI multiCoreAPI) {
        super(var0);
        this.multiCoreAPI = multiCoreAPI;
    }

    @Override
    public void a(PacketLoginInListener var0) {
        MultiPacketLoginInEncryptionBeginHandler multiLoginRequestInitialHandler =
                new MultiPacketLoginInEncryptionBeginHandler((LoginListener) var0, multiCoreAPI);
        try {
            multiLoginRequestInitialHandler.handle(this);
        } catch (Throwable e) {
            multiLoginRequestInitialHandler.getLoginListener().disconnect(multiCoreAPI.getLanguageHandler().getMessage("bukkit_auth_error"));
            LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
        }
    }
}
