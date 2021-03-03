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

import java.util.Map;
import java.util.concurrent.Callable;

public class AuthTask<T> implements Callable<T> {
    YggdrasilServiceEntry yggdrasilServiceEntry;
    Map<String, String> arg;
    Class<T> clazz;
    Gson gson;

    public AuthTask(YggdrasilServiceEntry yggdrasilServiceEntry, Map<String, String> arg, Class<T> clazz, Gson gson) {
        this.yggdrasilServiceEntry = yggdrasilServiceEntry;
        this.arg = arg;
        this.clazz = clazz;
        this.gson = gson;
    }

    @Override
    public T call() throws Exception {
        String url = yggdrasilServiceEntry.buildUrlStr(arg);
        String result;
        if (yggdrasilServiceEntry.isPostMode()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", arg.get("username"));
            jsonObject.addProperty("serverId", arg.get("serverId"));
            String context = jsonObject.toString();
            result = HttpGetter.httpPost(url, context);
        } else {
            result = HttpGetter.httpGet(url);
        }
        return gson.fromJson(result, clazz);
    }
}
