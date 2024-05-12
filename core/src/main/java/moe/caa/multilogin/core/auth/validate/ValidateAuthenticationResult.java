package moe.caa.multilogin.core.auth.validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.profile.GameProfile;

/**
 * 游戏内验证结果
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ValidateAuthenticationResult {
    private final Reason reason;
    private final GameProfile inGameProfile;
    private final String disallowedMessage;


    /**
     * 返回一个允许登录的结果
     */
    public static ValidateAuthenticationResult ofAllowed(GameProfile response) {
        return new ValidateAuthenticationResult(Reason.ALLOWED, response, null);
    }

    /**
     * 返回一个不允许登录的结果
     */
    public static ValidateAuthenticationResult ofDisallowed(String disallowedMessage) {
        return new ValidateAuthenticationResult(Reason.DISALLOWED, null, disallowedMessage);
    }

    public enum Reason {
        // 允许登录
        ALLOWED,
        // 不允许登录
        DISALLOWED;
    }
}
