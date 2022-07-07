package moe.caa.multilogin.core.auth.validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ValidateAuthenticationResult {
    private final Reason reason;
    private final HasJoinedResponse inGameProfile;
    private final String disallowedMessage;

    public static ValidateAuthenticationResult ofAllowed(HasJoinedResponse response) {
        return new ValidateAuthenticationResult(Reason.ALLOWED, response, null);
    }

    public static ValidateAuthenticationResult ofDisallowed(String disallowedMessage) {
        return new ValidateAuthenticationResult(Reason.DISALLOWED, null, disallowedMessage);
    }

    public enum Reason {
        ALLOWED,
        DISALLOWED;
    }
}
