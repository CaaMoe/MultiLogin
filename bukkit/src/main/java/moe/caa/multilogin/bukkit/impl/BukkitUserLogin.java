package moe.caa.multilogin.bukkit.impl;

import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.BaseUserLogin;

/**
 * 早知道要做那么多适配，就不弄这个接口了<br>
 * 你妈的，为什么
 */
public class BukkitUserLogin extends BaseUserLogin {

    @Getter
    private String kickMessage;

    @Getter
    private HasJoinedResponse response;

    public BukkitUserLogin(String username, String serverId, String ip) {
        super(username, serverId, ip);
    }

    @Override
    public void disconnect(String message) {
        this.kickMessage = message;
    }

    @Override
    public void finish(HasJoinedResponse response) {
        this.response = response;
    }

    public boolean isAllowed() {
        return kickMessage == null && response != null;
    }
}
