package moe.caa.multilogin.core.auth.validate;

import lombok.Data;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;

/**
 * 游戏内验证消息上下文
 */
@Data
public class ValidateContext {
    private final BaseServiceAuthenticationResult baseServiceAuthenticationResult;

    private final GameProfile inGameProfile;
    private String disallowMessage;
    private boolean needWait;
    private boolean onlineNameUpdated = false;


    protected ValidateContext(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
        this.baseServiceAuthenticationResult = baseServiceAuthenticationResult;
        this.inGameProfile = baseServiceAuthenticationResult.getResponse().clone();
    }
}
