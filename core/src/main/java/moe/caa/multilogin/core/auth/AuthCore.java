package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.FutureTask;

public class AuthCore {

    public static <T> Object yggAuth(String name, String serverId) throws SQLException {
        return yggAuth(name, serverId, null);
    }

    public static <T> AuthResult<T> yggAuth(String name, String serverId, String ip) throws SQLException {
        MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_START.getMessage(name, serverId, ip));

        List<List<YggdrasilService>> order = Verifier.getVeriOrder(name);
        boolean down = false;
        boolean haveService = false;

        for (List<YggdrasilService> entries : order) {
            if (entries.size() != 0) haveService = true;
            AuthResult<T> result = authWithTasks(entries, name, serverId, ip);
            if (result != null && result.isSuccess()) {
                MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_END_ALLOW.getMessage(name, result.service.name, result.service.path));
                return result;
            }
            if (result != null && result.err == AuthFailedEnum.SERVER_DOWN) {
                down = true;
            }
        }
        AuthFailedEnum failedEnum = down ? AuthFailedEnum.SERVER_DOWN : haveService ? AuthFailedEnum.VALIDATION_FAILED : AuthFailedEnum.NO_SERVICE;
        MultiLogger.log(LoggerLevel.DEBUG, LanguageKeys.DEBUG_LOGIN_END_DISALLOW.getMessage(name, failedEnum.name()));
        return new AuthResult<>(failedEnum);
    }

    private static <T> AuthResult<T> authWithTasks(List<YggdrasilService> services, String name, String serverId, String ip) {
        AuthResult<T> getResult = null;
        List<FutureTask<AuthResult<T>>> tasks = new ArrayList<>();
        long endTime = System.currentTimeMillis() + MultiCore.servicesTimeOut;
        for (YggdrasilService entry : services) {
            if (entry == null) continue;
            FutureTask<AuthResult<T>> task = new FutureTask<>(new AuthTask<>(entry, name, serverId, ip));
            MultiCore.plugin.getSchedule().runTaskAsync(task);
            tasks.add(task);
        }

        while (tasks.size() != 0 && endTime > System.currentTimeMillis()) {
            Iterator<FutureTask<AuthResult<T>>> itr = tasks.iterator();
            while (itr.hasNext()) {
                FutureTask<AuthResult<T>> task = itr.next();
                if (!task.isDone()) continue;
                try {
                    getResult = task.get();
                } catch (Exception ignored) {
                } finally {
                    itr.remove();
                }
            }
            if (getResult != null && getResult.isSuccess()) break;
        }
        for (FutureTask<AuthResult<T>> future : tasks) {
            future.cancel(true);
        }
        return getResult;
    }
}
