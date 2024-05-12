package moe.caa.multilogin.api.internal.auth;

import moe.caa.multilogin.api.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

/**
 * 验证结果
 */
@ApiStatus.Internal
public interface AuthResult {

    /**
     * 返回最终验证通过的游戏档案数据
     *
     * @return 最终验证通过的游戏档案数据
     */
    GameProfile getResponse();

    /**
     * 返回最终验证不通过的踢出提示
     *
     * @return 最终验证不通过的踢出提示
     */
    String getKickMessage();

    /**
     * 返回登录结果
     *
     * @return 登录结果
     */
    Result getResult();

    enum Result {
        ALLOW,
        DISALLOW_BY_YGGDRASIL_AUTHENTICATOR,
        DISALLOW_BY_VALIDATE_AUTHENTICATOR,
        ERROR
    }
}
