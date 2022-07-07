package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class YggdrasilAuthenticationResult {
    private final Reason reason;
    private final GameProfile response;
    private final int yggdrasilId;
    private final YggdrasilServiceConfig yggdrasilServiceConfig;

    protected static YggdrasilAuthenticationResult ofAllowed(GameProfile response, int yggdrasilId, YggdrasilServiceConfig yggdrasilServiceConfig) {
        return new YggdrasilAuthenticationResult(Reason.ALLOWED, response, yggdrasilId, yggdrasilServiceConfig);
    }

    protected static YggdrasilAuthenticationResult ofServerBreakdown() {
        return new YggdrasilAuthenticationResult(Reason.SERVER_BREAKDOWN, null, -1, null);
    }

    protected static YggdrasilAuthenticationResult ofValidationFailed() {
        return new YggdrasilAuthenticationResult(Reason.VALIDATION_FAILED, null, -1, null);
    }

    protected static YggdrasilAuthenticationResult ofNoService() {
        return new YggdrasilAuthenticationResult(Reason.NO_SERVICE, null, -1, null);
    }

    public enum Reason {
        ALLOWED,
        SERVER_BREAKDOWN,
        VALIDATION_FAILED,
        NO_SERVICE;
    }
}
