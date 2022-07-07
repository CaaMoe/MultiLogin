package moe.caa.multilogin.core.auth.validate;

import lombok.Data;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;

@Data
public class ValidateContext {
    private final String username;
    private final String serverId;
    private final String ip;
    private final YggdrasilAuthenticationResult yggdrasilAuthenticationResult;

    private final HasJoinedResponse inGameProfile;
    private String disallowMessage;

    protected ValidateContext(String username, String serverId, String ip, YggdrasilAuthenticationResult yggdrasilAuthenticationResult) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.yggdrasilAuthenticationResult = yggdrasilAuthenticationResult;
        this.inGameProfile = yggdrasilAuthenticationResult.getResponse().clone();
    }
}
