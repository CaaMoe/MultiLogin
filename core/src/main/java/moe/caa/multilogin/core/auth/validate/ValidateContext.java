package moe.caa.multilogin.core.auth.validate;

import lombok.Data;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;

/**
 * 游戏内验证消息上下文
 */
@Data
public class ValidateContext {
    private final String username;
    private final String serverId;
    private final String ip;
    private final YggdrasilAuthenticationResult yggdrasilAuthenticationResult;

    private final GameProfile inGameProfile;
    private String disallowMessage;

    private boolean needWait;

    protected ValidateContext(String username, String serverId, String ip, YggdrasilAuthenticationResult yggdrasilAuthenticationResult) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.yggdrasilAuthenticationResult = yggdrasilAuthenticationResult;
        this.inGameProfile = yggdrasilAuthenticationResult.getResponse().clone();
    }
}
