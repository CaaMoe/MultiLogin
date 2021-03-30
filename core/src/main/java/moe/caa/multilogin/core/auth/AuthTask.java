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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;
import moe.caa.multilogin.core.http.HttpGetter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Yggdrasil验证
 *
 * @param <T> 数据类型
 */
public class AuthTask<T> implements Callable<AuthResult<T>> {
    private static Type type;
    private static Gson gson;
    private final YggdrasilServiceEntry yggdrasilServiceEntry;
    private final Map<String, String> arg;

    public AuthTask(YggdrasilServiceEntry yggdrasilServiceEntry, Map<String, String> arg) {
        this.yggdrasilServiceEntry = yggdrasilServiceEntry;
        this.arg = arg;
    }

    /**
     * 启动必须调用的函数
     *
     * @param type 序列化后类型
     * @param gson 反序列化用的gson
     */
    public static void setServicePair(Type type, Gson gson) {
        AuthTask.type = type;
        AuthTask.gson = gson;
    }

    @Override
    public AuthResult<T> call() {
        AuthResult<T> authResult;
        try {
            String url = yggdrasilServiceEntry.buildUrlStr(arg);
            String result;
            if (yggdrasilServiceEntry.isPostMode()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", arg.get("username"));
                jsonObject.addProperty("serverId", arg.get("serverId"));
                String context = jsonObject.toString();
                result = HttpGetter.httpPost(url, context, yggdrasilServiceEntry.getAuthRetry());
            } else {
                result = HttpGetter.httpGet(url, yggdrasilServiceEntry.getAuthRetry());
            }
            T get = gson.fromJson(result, type);
            authResult = new AuthResult<>(get, yggdrasilServiceEntry);
        } catch (Exception e) {
            authResult = new AuthResult<>(AuthErrorEnum.SERVER_DOWN, yggdrasilServiceEntry);
            authResult.setThrowable(e);
        }
        return authResult;
    }
}
