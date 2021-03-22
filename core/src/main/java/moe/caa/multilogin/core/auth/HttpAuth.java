/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.HttpAuth
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 处理Yggdrasil验证请求的类
 */
public class HttpAuth {

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * 进行Yggdrasil验证，将会按name生成Yggdrasil访问顺序并且进行验证
     *
     * @param name 玩家的name，作为Yggdrasil的排序依据
     * @param arg  GET的请求参数，为“hasJoined?username=%s&serverId=%s%s”
     * @param <T>  请求返回的数据对象
     * @return 验证结果
     * @throws SQLException 数据查询失败
     */
    public static <T> AuthResult<T> yggAuth(String name, Map<String, String> arg) throws SQLException {
        List<List<YggdrasilServiceEntry>> order = Verifier.getVeriOrder(name);
        boolean down = false;

        for (List<YggdrasilServiceEntry> entries : order) {
//            分批验证 根据超时进行分批
            AuthResult<T> result = authWithTasks(entries, arg);
            if (result.isSuccess()) {
                return result;
            }
            if (result.getErr() == AuthErrorEnum.SERVER_DOWN) {
                down = true;
            }
        }
        return new AuthResult<>(down ? AuthErrorEnum.SERVER_DOWN : AuthErrorEnum.VALIDATION_FAILED);
    }

    /**
     * 进行Yggdrasil验证，同时访问指定的所有Yggdrasil服务器
     *
     * @param serviceEntryList 指定的Yggdrasil服务器列表
     * @param arg              GET的请求参数，为“hasJoined?username=%s&serverId=%s%s”
     * @param <T>              请求返回的数据对象
     * @return 验证结果
     */
    private static <T> AuthResult<T> authWithTasks(List<YggdrasilServiceEntry> serviceEntryList, Map<String, String> arg) {
        AuthResult<T> getResult = null;
//        任务列表
        List<FutureTask<AuthResult<T>>> tasks = new LinkedList<>();
        long endTime = System.currentTimeMillis() + PluginData.getTimeOut();

        for (YggdrasilServiceEntry entry : serviceEntryList) {
            if (entry == null) continue;
//            AuthTask进行具体验证
            FutureTask<AuthResult<T>> task = new FutureTask<>(new AuthTask<>(entry, arg));
            threadPool.execute(task);
//            执行后放在列表里
            tasks.add(task);
        }
//        没有可验证服务器
        if (tasks.size() == 0) return new AuthResult<>(AuthErrorEnum.NO_SERVER);
//        时间限制
        while (endTime > System.currentTimeMillis()) {
            for (Future<AuthResult<T>> task : tasks) {
                if (!task.isDone()) continue;
                try {
                    getResult = task.get();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            if (getResult != null && getResult.isSuccess()) break;
        }
        for (FutureTask<AuthResult<T>> future : tasks) {
            future.cancel(true);
        }
        return getResult;
    }

    public static void shutDown() {
        threadPool.shutdown();
    }
}
