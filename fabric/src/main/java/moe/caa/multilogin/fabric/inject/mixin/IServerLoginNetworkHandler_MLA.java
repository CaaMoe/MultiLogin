package moe.caa.multilogin.fabric.inject.mixin;

import com.mojang.authlib.GameProfile;

/**
 * 丰富指令源方法
 */
public interface IServerLoginNetworkHandler_MLA {
    GameProfile mlHandler_getGameProfile();
}
