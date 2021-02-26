package moe.caa.multilogin.core.auth;

import java.util.UUID;

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

    public String getFAIL_MSG() {
        return FAIL_MSG;
    }

    public UUID getREDIRECT_UUID() {
        return REDIRECT_UUID;
    }

}
