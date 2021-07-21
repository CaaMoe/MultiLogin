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
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class AuthTask<T> implements Callable<AuthResult<T>> {
    private final YggdrasilService service;
    private final String username;
    private final String serverId;
    private final String ip;
    private final MultiCore core;
    private final CountDownLatch countDownLatch;

    public AuthTask(YggdrasilService service, String username, String serverId, String ip, MultiCore core, CountDownLatch countDownLatch) {
        this.service = service;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.core = core;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public AuthResult<T> call() {
        AuthResult<T> authResult;
        try {
            String result;
            if (service.getBody().isPostMode()) {
                result = HttpUtil.httpPostJson(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), service.buildPostContent(username, serverId, ip), core.servicesTimeOut, service.getAuthRetry());
            } else {
                result = HttpUtil.httpGet(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), core.servicesTimeOut, service.getAuthRetry());
            }
            if (ValueUtil.notIsEmpty(result)) {
                core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_ALLOW.getMessage(core, username, service.getName(), service.getPath()));
            } else {
                core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_DISALLOW.getMessage(core, username, service.getName(), service.getPath()));
            }

            T content = core.plugin.getAuthGson().fromJson(result, core.plugin.authResultType());
            authResult = new AuthResult<>(content, service);
        } catch (Exception e) {
            core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_SERVER_DOWN.getMessage(core, username, service.getName(), service.getPath()));
            authResult = new AuthResult<>(AuthFailedEnum.SERVER_DOWN, service);
            authResult.throwable = e;
        } finally {
//            计数
            countDownLatch.countDown();
        }
        if (authResult.isSuccess()) {
            while (countDownLatch.getCount() != 0) {
//                强制结束
                countDownLatch.countDown();
            }
        }
        return authResult;
    }
}
