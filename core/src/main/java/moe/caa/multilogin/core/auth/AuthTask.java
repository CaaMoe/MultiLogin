package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.concurrent.Callable;

public class AuthTask<T> implements Callable<AuthResult<T>> {
    private final YggdrasilService service;
    private final String username;
    private final String serverId;
    private final String ip;

    public AuthTask(YggdrasilService service, String username, String serverId, String ip) {
        this.service = service;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
    }

    @Override
    public AuthResult<T> call() {
        AuthResult<T> authResult;
        try {
            String result;
            if (service.body.postMode) {
                result = HttpUtil.httpPostJson(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), service.buildPostContent(username, serverId, ip), MultiCore.servicesTimeOut, service.authRetry);
            } else {
                result = HttpUtil.httpGet(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), MultiCore.servicesTimeOut, service.authRetry);
            }
            if (ValueUtil.notIsEmpty(result)) {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_ALLOW.getMessage(username, service.name, service.path));
            } else {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_DISALLOW.getMessage(username, service.name, service.path));
            }

            T content = MultiCore.plugin.getAuthGson().fromJson(result, MultiCore.plugin.authResultType());
            authResult = new AuthResult<>(content, service);
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_SERVER_DOWN.getMessage(username, service.name, service.path));
            authResult = new AuthResult<>(AuthFailedEnum.SERVER_DOWN, service);
            authResult.throwable = e;
        }
        return authResult;
    }
}
