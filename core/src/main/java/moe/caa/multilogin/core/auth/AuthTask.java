package moe.caa.multilogin.core.auth;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * 代表一个验证请求的线程
 */
@AllArgsConstructor
public class AuthTask implements Callable<HasJoinedResponse> {
    private final YggdrasilService service;
    private final String username;
    private final String serverId;
    private final String ip;

    @Override
    public HasJoinedResponse call() throws Exception {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Verifying player login request...(service: %s, username: %s, serverId: %s, ip: %s)",
                service.getPathString(), username, serverId, ip == null ? "unknown" : ip));
        try {
            if (service.isPostMode()) return MultiCore.getGson().fromJson(
                    HttpUtil.httpPostJson(
                            new URL(service.buildUrl(username, serverId, ip)),
                            service.buildPostContent(username, serverId, ip),
                            "application/json",
                            (int) MultiCore.getCore().getServicesTimeOut(),
                            service.getAuthRetry()
                    ),
                    HasJoinedResponse.class
            );
            return MultiCore.getGson().fromJson(
                    HttpUtil.httpGet(
                            new URL(service.buildUrl(username, serverId, ip)),
                            (int) MultiCore.getCore().getServicesTimeOut(),
                            service.getAuthRetry()
                    ),
                    HasJoinedResponse.class
            );
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Verification failed. (service: %s, username: %s, serverId: %s, ip: %s)", service.getPathString(), username, serverId, ip == null ? "unknown" : ip), e);
            throw e;
        }
    }
}
