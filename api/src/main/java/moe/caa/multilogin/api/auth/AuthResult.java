package moe.caa.multilogin.api.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证结果
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResult {
    @Getter
    private final String kickMessage;
    @Getter
    private final GameProfile response;
    @Getter
    private final boolean allowed;

    /**
     * 构建一个允许登录的验证结果
     *
     * @param response 游戏档案
     */
    public static AuthResult ofAllowed(GameProfile response) {
        return new AuthResult(null, response, true);
    }

    /**
     * 构建一个不允许登录的验证结果
     *
     * @param kickMessage 踢出消息
     */
    public static AuthResult ofDisallowed(String kickMessage) {
        return new AuthResult(kickMessage, null, false);
    }
}
