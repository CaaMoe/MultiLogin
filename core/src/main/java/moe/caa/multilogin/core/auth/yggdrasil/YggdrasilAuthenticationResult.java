package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

/**
 * HasJoined 验证结果
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class YggdrasilAuthenticationResult {
    private final Reason reason;
    private final GameProfile response;
    private final int yggdrasilId;
    private final YggdrasilServiceConfig yggdrasilServiceConfig;

    /**
     * 提供一个允许登录的结果
     */
    protected static YggdrasilAuthenticationResult ofAllowed(GameProfile response, int yggdrasilId, YggdrasilServiceConfig yggdrasilServiceConfig) {
        return new YggdrasilAuthenticationResult(Reason.ALLOWED, response, yggdrasilId, yggdrasilServiceConfig);
    }

    /**
     * 提供一个服务器宕机的结果
     */
    protected static YggdrasilAuthenticationResult ofServerBreakdown() {
        return new YggdrasilAuthenticationResult(Reason.SERVER_BREAKDOWN, null, -1, null);
    }

    /**
     * 提供一个验证失败的结果
     */
    protected static YggdrasilAuthenticationResult ofValidationFailed() {
        return new YggdrasilAuthenticationResult(Reason.VALIDATION_FAILED, null, -1, null);
    }

    /**
     * 提供一个没有验证服务器的结果
     */
    protected static YggdrasilAuthenticationResult ofNoService() {
        return new YggdrasilAuthenticationResult(Reason.NO_SERVICE, null, -1, null);
    }

    public enum Reason {
        // 通过
        ALLOWED,
        // 服务器宕机或破坏
        SERVER_BREAKDOWN,
        // 验证失败
        VALIDATION_FAILED,
        // 没有验证服务器
        NO_SERVICE;
    }
}
