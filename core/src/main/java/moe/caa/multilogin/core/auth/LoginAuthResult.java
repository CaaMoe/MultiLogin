package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.api.internal.auth.AuthResult;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;
import moe.caa.multilogin.core.auth.service.yggdrasil.UnmodifiableGameProfile;
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationResult;

@Getter
public class LoginAuthResult implements AuthResult {
    private final UnmodifiableGameProfile response;
    private final String kickMessage;
    private final Result result;
    private final BaseServiceAuthenticationResult baseServiceAuthenticationResult;
    private final ValidateAuthenticationResult validateAuthenticationResult;

    protected LoginAuthResult(UnmodifiableGameProfile response, String kickMessage, Result result,
                              BaseServiceAuthenticationResult baseServiceAuthenticationResult, ValidateAuthenticationResult validateAuthenticationResult) {
        this.response = response;
        this.kickMessage = kickMessage;
        this.result = result;
        this.baseServiceAuthenticationResult = baseServiceAuthenticationResult;
        this.validateAuthenticationResult = validateAuthenticationResult;
    }

    public static LoginAuthResult ofDisallowedByYggdrasilAuthenticator(YggdrasilAuthenticationResult yggdrasilAuthenticationResult, String kickMessage) {
        return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_YGGDRASIL_AUTHENTICATOR, yggdrasilAuthenticationResult, null);
    }

    public static LoginAuthResult ofDisallowedByValidateAuthenticator(BaseServiceAuthenticationResult baseServiceAuthenticationResult,
                                                                      ValidateAuthenticationResult validateAuthenticationResult,
                                                                      String kickMessage) {
        return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_VALIDATE_AUTHENTICATOR, baseServiceAuthenticationResult, validateAuthenticationResult);
    }

    public static LoginAuthResult ofAllowed(BaseServiceAuthenticationResult baseServiceAuthenticationResult,
                                            ValidateAuthenticationResult validateAuthenticationResult,
                                            GameProfile gameProfile) {
        return new LoginAuthResult(UnmodifiableGameProfile.unmodifiable(gameProfile), null, Result.ALLOW, baseServiceAuthenticationResult, validateAuthenticationResult);
    }
}
