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

import java.util.concurrent.CountDownLatch;

public class AuthTask<T> implements Runnable {
    private final YggdrasilService service;
    private final String username;
    private final String serverId;
    private final String ip;
    private final CountDownLatch countDownLatch;
    private AuthResult<T> authResult;

    public AuthTask(YggdrasilService service, String username, String serverId, String ip, CountDownLatch countDownLatch) {
        this.service = service;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            String result;
            if (service.getBody().isPostMode()) {
                result = HttpUtil.httpPostJson(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), service.buildPostContent(username, serverId, ip), MultiCore.getInstance().servicesTimeOut, service.getAuthRetry());
            } else {
                result = HttpUtil.httpGet(HttpUtil.getUrlFromString(service.buildUrl(username, serverId, ip)), MultiCore.getInstance().servicesTimeOut, service.getAuthRetry());
            }
            if (ValueUtil.notIsEmpty(result)) {
                MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_ALLOW.getMessage(username, service.getName(), service.getPath()));
            } else {
                MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_DISALLOW.getMessage(username, service.getName(), service.getPath()));
            }

            T content = MultiCore.getPlugin().getAuthGson().fromJson(result, MultiCore.getPlugin().authResultType());
            authResult = new AuthResult<>(content, service);
        } catch (Exception e) {
            MultiCore.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_AUTH_TASK_SERVER_DOWN.getMessage(username, service.getName(), service.getPath()));
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
    }

    public AuthResult<T> getAuthResult() {
        return authResult;
    }
}
