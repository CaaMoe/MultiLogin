package moe.caa.multilogin.bukkit.impl;

import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.BaseUserLogin;

import java.util.concurrent.CountDownLatch;

/**
 * 早知道要做那么多适配，就不弄这个接口了
 */
public class BukkitUserLogin extends BaseUserLogin {
    private final CountDownLatch latch;

    @Getter
    private String kickMessage;

    @Getter
    private HasJoinedResponse response;

    public BukkitUserLogin(String username, String serverId, String ip, CountDownLatch latch) {
        super(username, serverId, ip);
        this.latch = latch;
    }

    @Override
    public void disconnect(String message) {
        this.kickMessage = message;
        latch.countDown();
    }

    @Override
    public void finish(HasJoinedResponse response) {
        this.response = response;
        latch.countDown();
    }

    public boolean isAllowed() {
        return kickMessage == null && response != null;
    }
}
