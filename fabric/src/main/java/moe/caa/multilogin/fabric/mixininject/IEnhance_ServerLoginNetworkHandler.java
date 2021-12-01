package moe.caa.multilogin.fabric.mixininject;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;

public interface IEnhance_ServerLoginNetworkHandler {
    GameProfile multiLogin_getProfile();

    void multiLogin_setProfile(GameProfile profile);

    default ServerLoginNetworkHandler getAsServerLoginNetworkHandler() {
        return (ServerLoginNetworkHandler) this;
    }
}
