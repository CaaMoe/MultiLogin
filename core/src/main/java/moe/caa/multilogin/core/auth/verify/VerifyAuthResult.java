package moe.caa.multilogin.core.auth.verify;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moe.caa.multilogin.core.user.User;

/**
 * 用户核查登入结果
 */
@Getter
@Setter
@ToString
public class VerifyAuthResult {
    private String kickMessage;
    private User user;

    public static VerifyAuthResult generateAllowResult(User user) {
        VerifyAuthResult result = new VerifyAuthResult();
        result.user = user;
        return result;
    }

    public static VerifyAuthResult generateKickResult(String kickMessage) {
        VerifyAuthResult result = new VerifyAuthResult();
        result.kickMessage = kickMessage;
        return result;
    }

    public boolean isFailed() {
        return kickMessage != null;
    }
}
