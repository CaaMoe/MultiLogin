package moe.caa.multilogin.api.data;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 表示一个使用猫踢螺钉登录的玩家的登录数据
 */
@ApiStatus.NonExtendable
public interface MultiLoginPlayerData {

    /**
     * 获取这个玩家的在线游戏档案
     * @return 这个玩家的在线游戏档案
     */
    @NotNull GameProfile getOnlineProfile();

    /**
     * 获取这个玩家使用的验证服务器
     * @return 这个玩家使用的验证服务器
     */
    @NotNull IService getLoginService();
}
