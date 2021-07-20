/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.AuthTask
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

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
            if (service.getBody().getPostMode()) {
                result = HttpUtil.httpPostJson(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), service.buildPostContent(username, serverId, ip), MultiCore.servicesTimeOut, service.getAuthRetry());
            } else {
                result = HttpUtil.httpGet(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), MultiCore.servicesTimeOut, service.getAuthRetry());
            }
            if (ValueUtil.notIsEmpty(result)) {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_ALLOW.getMessage(username, service.getName(), service.getPath()));
            } else {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_DISALLOW.getMessage(username, service.getName(), service.getPath()));
            }

            T content = MultiCore.plugin.getAuthGson().fromJson(result, MultiCore.plugin.authResultType());
            authResult = new AuthResult<>(content, service);
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_SERVER_DOWN.getMessage(username, service.getName(), service.getPath()));
            authResult = new AuthResult<>(AuthFailedEnum.SERVER_DOWN, service);
            authResult.throwable = e;
        }
        return authResult;
    }
}
