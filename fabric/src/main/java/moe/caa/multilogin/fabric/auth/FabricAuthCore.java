package moe.caa.multilogin.fabric.auth;

import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.fabric.main.MultiLoginFabricPluginBootstrap;

import java.net.InetAddress;

@AllArgsConstructor
public class FabricAuthCore {
    private final MultiLoginFabricPluginBootstrap bootstrap;

    public MultiLoginGameProfile doAuth(GameProfile user, String serverId, InetAddress address) {
        //FabricUserLogin login = new FabricUserLogin();
        return null;
    }
}
