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
import java.util.HashMap;
import java.util.Iterator;
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
            AuthResult<T> result = yggAuth(entries, arg);
            if (result.getErr() == null) {
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
    private static <T> AuthResult<T> yggAuth(List<YggdrasilServiceEntry> serviceEntryList, Map<String, String> arg) {
        T getResult = null;
//        服务器关闭
        boolean down = false;
//        任务列表
        Map<Future<T>, YggdrasilServiceEntry> tasks = new HashMap<>();
//        完成任务的task
        Future<T> done = null;
        long endTime = System.currentTimeMillis() + PluginData.getTimeOut();

        for (YggdrasilServiceEntry entry : serviceEntryList) {
            if (entry == null) continue;
//            请求json数据 转换成需要的对象
            FutureTask<T> task = new FutureTask<>(new AuthTask<>(entry, arg));
            threadPool.execute(task);
//            执行后放在列表里
            tasks.put(task, entry);
        }

        dos:
        while (endTime > System.currentTimeMillis() && tasks.size() != 0) {
            Iterator<Future<T>> itr = tasks.keySet().iterator();
            while (itr.hasNext()) {
                Future<T> task = itr.next();
                if (task.isDone()) {
                    try {
                        getResult = task.get();
                    } catch (Exception ignored) {
                        down = true;
//                        ignored.printStackTrace();
                    }
                    if (getResult != null) {
//                        成功结果
                        done = task;
                        break dos;
                    }
//                    完成移除
                    itr.remove();
                }
            }
        }
        for (Future<T> future : tasks.keySet()) {
            future.cancel(true);
        }
        if (getResult == null) {
            if (down)
                return new AuthResult<>(AuthErrorEnum.SERVER_DOWN);
            return new AuthResult<>(AuthErrorEnum.VALIDATION_FAILED);
        }
        return new AuthResult<>(getResult, tasks.get(done));
    }

    public static void shutDown() {
        threadPool.shutdown();
    }
}
