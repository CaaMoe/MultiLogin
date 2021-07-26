/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.AuthCore
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AuthCore {
    //    单开一个验证服务用的池子 区分
    ExecutorService authExecutor = Executors.newCachedThreadPool();

    public final MultiCore core;

    public AuthCore(MultiCore core) {
        this.core = core;
    }

    public <T> Object yggAuth(String name, String serverId) throws SQLException, InterruptedException {
        return yggAuth(name, serverId, null);
    }

    public <T> AuthResult<T> yggAuth(String name, String serverId, String ip) throws SQLException, InterruptedException {
        core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_START.getMessage(core, name, serverId, ip));
//        顺序获取
        List<List<YggdrasilService>> order = core.getVerifier().getVeriOrder(name);
//        服务器关闭
        boolean down = false;
        boolean haveService = false;
//        异常 必须保留
        Throwable throwable = null;

        for (List<YggdrasilService> entries : order) {
//            没有服务直接下一个
            if (entries.size() != 0) {
                haveService = true;
            } else {
                continue;
            }

//            获取验证结果
            AuthResult<T> result = authWithTasks(entries, name, serverId, ip);
            if (result != null && result.isSuccess()) {
                core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_END_ALLOW.getMessage(core, name, result.service.getName(), result.service.getPath()));
                return result;
            }
            if (result != null && result.err == AuthFailedEnum.SERVER_DOWN) {
                down = true;
            }
//            异常传出 必须留
            if (result != null && result.throwable != null) throwable = result.throwable;
        }
//        重构结果信息
        AuthFailedEnum failedEnum = down ? AuthFailedEnum.SERVER_DOWN : haveService ? AuthFailedEnum.VALIDATION_FAILED : AuthFailedEnum.NO_SERVICE;
        core.getLogger().log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_END_DISALLOW.getMessage(core, name, failedEnum.name()));
        return new AuthResult<>(failedEnum, throwable);
    }

    private <T> AuthResult<T> authWithTasks(List<YggdrasilService> services, String name, String serverId, String ip) throws InterruptedException {
        AuthResult<T> getResult = null;
//        放线程的
        List<Future<?>> threads = new ArrayList<>();
//        放结果的
        List<AuthTask<T>> tasks = new ArrayList<>();
//        同步用的
        CountDownLatch countDownLatch = new CountDownLatch(services.size());
        for (YggdrasilService entry : services) {
            if (entry == null) continue;
//            创建进程
            AuthTask<T> task = new AuthTask<>(entry, name, serverId, ip, core, countDownLatch);
            Future<?> thread = authExecutor.submit(task);
            tasks.add(task);
            threads.add(thread);
        }
//        等待 不使用循环 减少cpu消耗
        countDownLatch.await(core.servicesTimeOut, TimeUnit.MILLISECONDS);
//        全部关闭
        threads.parallelStream().forEach(t -> t.cancel(true));
        for (AuthTask<T> task : tasks) {
//            由于强制终结的存在 还是会有没完成的
//            没结果
            AuthResult<T> wResult = task.getAuthResult();
            if (wResult == null) continue;
//            有结果 但是当前没结果 放进去
            if (getResult == null) getResult = wResult;
            if (wResult.isSuccess()) {
//                结果成功放进去
                getResult = wResult;
                break;
            }
        }
        return getResult;
    }
}
