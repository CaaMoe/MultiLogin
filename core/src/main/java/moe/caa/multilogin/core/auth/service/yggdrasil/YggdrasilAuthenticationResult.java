package moe.caa.multilogin.core.auth.service.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;
import moe.caa.multilogin.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;

/**
 * HasJoined 验证结果
 */
@Getter
@ToString
public class YggdrasilAuthenticationResult extends BaseServiceAuthenticationResult {
    private final Reason reason;

    public YggdrasilAuthenticationResult(Reason reason, GameProfile response, BaseYggdrasilServiceConfig serviceConfig) {
        super(response, serviceConfig);
        this.reason = reason;
    }

    /**
     * 提供一个允许登录的结果
     */
    protected static YggdrasilAuthenticationResult ofAllowed(GameProfile response, BaseYggdrasilServiceConfig serviceConfig) {
        return new YggdrasilAuthenticationResult(Reason.ALLOWED, response, serviceConfig);
    }

    /**
     * 提供一个服务器宕机的结果
     */
    protected static YggdrasilAuthenticationResult ofServerBreakdown() {
        return new YggdrasilAuthenticationResult(Reason.SERVER_BREAKDOWN, null, null);
    }

    /**
     * 提供一个验证失败的结果
     */
    protected static YggdrasilAuthenticationResult ofValidationFailed() {
        return new YggdrasilAuthenticationResult(Reason.VALIDATION_FAILED, null, null);
    }

    /**
     * 提供一个没有验证服务器的结果
     */
    protected static YggdrasilAuthenticationResult ofNoService() {
        return new YggdrasilAuthenticationResult(Reason.NO_SERVICE, null, null);
    }

    @Override
    public boolean isAllowed() {
        return reason == Reason.ALLOWED;
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
