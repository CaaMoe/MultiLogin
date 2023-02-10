package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationResult;
import moe.caa.multilogin.core.auth.yggdrasil.UnmodifiableGameProfile;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;

@Getter
public class LoginAuthResult implements AuthResult {
    private final UnmodifiableGameProfile response;
    private final String kickMessage;
    private final Result result;
    private final YggdrasilAuthenticationResult yggdrasilAuthenticationResult;
    private final ValidateAuthenticationResult validateAuthenticationResult;

    protected LoginAuthResult(UnmodifiableGameProfile response, String kickMessage, Result result,
                              YggdrasilAuthenticationResult yggdrasilAuthenticationResult, ValidateAuthenticationResult validateAuthenticationResult) {
        this.response = response;
        this.kickMessage = kickMessage;
        this.result = result;
        this.yggdrasilAuthenticationResult = yggdrasilAuthenticationResult;
        this.validateAuthenticationResult = validateAuthenticationResult;
    }

    public static LoginAuthResult ofDisallowedByYggdrasilAuthenticator(YggdrasilAuthenticationResult yggdrasilAuthenticationResult, String kickMessage) {
        return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_YGGDRASIL_AUTHENTICATOR, yggdrasilAuthenticationResult, null);
    }

    public static LoginAuthResult ofDisallowedByValidateAuthenticator(YggdrasilAuthenticationResult yggdrasilAuthenticationResult,
                                                                      ValidateAuthenticationResult validateAuthenticationResult,
                                                                      String kickMessage) {
        return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_VALIDATE_AUTHENTICATOR, yggdrasilAuthenticationResult, validateAuthenticationResult);
    }

    public static LoginAuthResult ofAllowed(YggdrasilAuthenticationResult yggdrasilAuthenticationResult,
                                            ValidateAuthenticationResult validateAuthenticationResult,
                                            GameProfile gameProfile) {
        return new LoginAuthResult(UnmodifiableGameProfile.unmodifiable(gameProfile), null, Result.ALLOW, yggdrasilAuthenticationResult, validateAuthenticationResult);
    }
}
