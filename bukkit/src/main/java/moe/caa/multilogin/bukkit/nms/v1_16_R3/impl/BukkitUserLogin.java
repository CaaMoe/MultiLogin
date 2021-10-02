package moe.caa.multilogin.bukkit.nms.v1_16_R3.impl;

import moe.caa.multilogin.core.impl.BaseUserLogin;
import net.minecraft.server.v1_16_R3.LoginListener;

public class BukkitUserLogin extends BaseUserLogin {
    private final LoginListener vanHandler;

    public BukkitUserLogin(LoginListener vanHandler, String username, String serverId, String ip) {
        super(username, serverId, ip);
        this.vanHandler = vanHandler;
    }

    @Override
    public void disconnect(String message) {
        vanHandler.disconnect(message);
    }

    @Override
    public void finish() {

    }
}
