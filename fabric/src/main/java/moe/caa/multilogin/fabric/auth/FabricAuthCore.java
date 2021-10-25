package moe.caa.multilogin.fabric.auth;

import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.fabric.impl.FabricUserLogin;
import moe.caa.multilogin.fabric.main.MultiLoginFabricModBootstrap;

import java.net.InetAddress;

@AllArgsConstructor
public class FabricAuthCore {
    private final MultiLoginFabricModBootstrap bootstrap;

    public GameProfile doAuth(GameProfile user, String serverId, InetAddress address) {
        //FabricUserLogin login = new FabricUserLogin();
        return null;
    }
}
