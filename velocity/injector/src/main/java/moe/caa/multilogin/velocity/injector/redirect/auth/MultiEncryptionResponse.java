package moe.caa.multilogin.velocity.injector.redirect.auth;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.main.MultiCoreAPI;
import moe.caa.multilogin.velocity.injector.handler.MultiInitialLoginSessionHandler;
import net.kyori.adventure.text.Component;

/**
 * EncryptionResponse 数据包处理
 */
@AllArgsConstructor
public class MultiEncryptionResponse extends EncryptionResponsePacket {
    private final MultiCoreAPI multiCoreAPI;

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        if (!(handler instanceof InitialLoginSessionHandler)) {
            return super.handle(handler);
        }
        MultiInitialLoginSessionHandler multiInitialLoginSessionHandler = new MultiInitialLoginSessionHandler(((InitialLoginSessionHandler) handler), multiCoreAPI);
        try {
            multiInitialLoginSessionHandler.handle(this);
        } catch (Throwable e) {
            if (multiInitialLoginSessionHandler.isEncrypted()) {
                multiInitialLoginSessionHandler.getInbound().disconnect(Component.text(multiCoreAPI.getLanguageHandler().getMessage("auth_error")));
            }
            multiInitialLoginSessionHandler.getMcConnection().close(true);
            LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
        }
        return true;
    }
}
