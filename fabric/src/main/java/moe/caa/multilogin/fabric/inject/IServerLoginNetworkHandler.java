package moe.caa.multilogin.fabric.inject;

import com.mojang.authlib.GameProfile;

public interface IServerLoginNetworkHandler {
    GameProfile getProfile();
}
