package moe.caa.multilogin.bungee.injector.redirect;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.bungee.injector.handler.MultiLoginRequestInitialHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.LoginRequest;

public class MultiLoginRequest extends LoginRequest {
    private final MultiCoreAPI multiCoreAPI;

    public MultiLoginRequest(MultiCoreAPI multiCoreAPI) {
        this.multiCoreAPI = multiCoreAPI;
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        if (!(handler instanceof InitialHandler)) {
            super.handle(handler);
            return;
        }
        MultiLoginRequestInitialHandler multiLoginRequestInitialHandler =
                new MultiLoginRequestInitialHandler(((InitialHandler) handler), multiCoreAPI);
        try {
            multiLoginRequestInitialHandler.handle(this);
        } catch (Throwable e) {
            multiLoginRequestInitialHandler.getInitialHandler().disconnect(new TextComponent(multiCoreAPI.getLanguageHandler().getMessage("auth_error")));
            LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
        }
    }
}
