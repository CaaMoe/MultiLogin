package moe.caa.multilogin.core.auth.verify;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户核查登入结果
 */
@AllArgsConstructor
@Getter
public class VerifyAuthResult {
    private final VerifyAuthReasonEnum reason;
    private final String kickMessage;

    /**
     * 身份验证是否有效且成功
     *
     * @return 验证是否成功
     */
    public boolean isSuccess() {
        return kickMessage == null && reason == VerifyAuthReasonEnum.PASS;
    }
}
