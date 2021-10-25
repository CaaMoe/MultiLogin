package moe.caa.multilogin.fabric.mixininject;

import com.mojang.authlib.GameProfile;

public interface IEnhance_ServerLoginNetworkHandler {
    GameProfile multiLogin_getProfile();

    void multiLogin_setProfile(GameProfile profile);
}
