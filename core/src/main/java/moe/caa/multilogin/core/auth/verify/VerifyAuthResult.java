package moe.caa.multilogin.core.auth.verify;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 用户核查登入结果
 */
@Getter
@Setter
public class VerifyAuthResult {
    private final VerifyAuthReasonEnum reason;
    private final String kickMessage;
    private String redirectName;
    private UUID redirectUuid;

    /**
     * 构建核查结果
     *
     * @param reason      结果枚举
     * @param kickMessage 踢出信息
     */
    public VerifyAuthResult(VerifyAuthReasonEnum reason, String kickMessage) {
        this.reason = reason;
        this.kickMessage = kickMessage;
    }

    /**
     * 构建核查结果
     *
     * @param reason 结果枚举
     */
    public VerifyAuthResult(VerifyAuthReasonEnum reason) {
        this.reason = reason;
        this.kickMessage = null;
    }

    /**
     * 身份验证是否有效且成功
     *
     * @return 验证是否成功
     */
    public boolean isSuccess() {
        return kickMessage == null && reason == VerifyAuthReasonEnum.PASS;
    }
}
