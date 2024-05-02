package moe.caa.multilogin.api.player;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 存储使用 MultiLogin 登录的玩家的登录数据
 */
@ApiStatus.NonExtendable
public interface MultiLoginPlayerData {

    /**
     * 返回玩家登录时从 Auth Service 中记录到的游戏档案
     * @return 玩家登录时从 Auth Service 中记录到的游戏档案
     */
    @NotNull
    GameProfile getLoginProfile();

    /**
     * 返回玩家所使用的 Auth Service
     * @return 玩家所使用的 Auth Service
     */
    @NotNull
    IService getAuthService();

    /**
     * 返回玩家在游戏内使用的游戏档案
     * @return 玩家在游戏内使用的游戏档案
     */
    @NotNull
    GameProfile getInGameProfile();
}