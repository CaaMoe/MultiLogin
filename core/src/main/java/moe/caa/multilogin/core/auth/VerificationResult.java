package moe.caa.multilogin.core.auth;

import java.util.UUID;

/**
 * 返回其他验证后的结果，包含白名单、重名检查等
 * 验证结果包含重定向的 UUID
 */
public class VerificationResult {
    public final String FAIL_MSG;
    public final UUID REDIRECT_UUID;

    public VerificationResult(String FAIL_MSG) {
        this.FAIL_MSG = FAIL_MSG;
        this.REDIRECT_UUID = null;
    }

    public VerificationResult(UUID REDIRECT_UUID) {
        this.FAIL_MSG = null;
        this.REDIRECT_UUID = REDIRECT_UUID;
    }
}