package moe.caa.multilogin.fabric.impl;

import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;

public class FabricUserLogin extends BaseUserLogin {
    private final ServerLoginNetworkHandler loginNetworkHandler;

    @Getter
    private HasJoinedResponse response;

    public FabricUserLogin(ServerLoginNetworkHandler loginNetworkHandler, String username, String serverId, String ip) {
        super(username, serverId, ip);
        this.loginNetworkHandler = loginNetworkHandler;
    }

    @Override
    public void disconnect(String message) {
        loginNetworkHandler.disconnect(new LiteralText(message));
    }

    @Override
    public void finish(HasJoinedResponse response) {
        this.response = response;
    }
}
