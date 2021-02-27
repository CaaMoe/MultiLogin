package moe.caa.multilogin.core.auth;

import java.util.UUID;

/**
 * 返回其他验证后的结果，包含白名单、重名检查等
 * 验证结果包含重定向的UUID
 */
public class VerificationResult {
    private final String FAIL_MSG;
    private final UUID REDIRECT_UUID;

    public VerificationResult(String FAIL_MSG) {
        this.FAIL_MSG = FAIL_MSG;
        this.REDIRECT_UUID = null;
    }

    public VerificationResult(UUID REDIRECT_UUID) {
        this.FAIL_MSG = null;
        this.REDIRECT_UUID = REDIRECT_UUID;
    }

    /**
     * 返回验证失败的理由，
     * @return 验证失败理由
     */
    public String getFAIL_MSG() {
        return FAIL_MSG;
    }

    /**
     * 获得重定向的UUID
     * @return 重定向的UUID，当getFAIL_MSG不为空时，此值为空
     */
    public UUID getREDIRECT_UUID() {
        return REDIRECT_UUID;
    }

}
