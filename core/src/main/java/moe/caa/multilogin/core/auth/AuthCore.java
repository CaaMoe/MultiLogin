package moe.caa.multilogin.core.auth;

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
        List<List<YggdrasilService>> order = Verifier.getVeriOrder(name);
        boolean down = false;

        for (List<YggdrasilService> entries : order){
            AuthResult<T> result = authWithTasks(entries, name, serverId, ip);
            if (result != null && result.isSuccess()) {
                return result;
            }
            if (result != null && result.err == AuthFailedEnum.SERVER_DOWN) {
                down = true;
            }
        }
        return new AuthResult<>(down ? AuthFailedEnum.SERVER_DOWN : AuthFailedEnum.VALIDATION_FAILED);
    }

    private static <T> AuthResult<T> authWithTasks(List<YggdrasilService> services, String name, String serverId, String ip){
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
                } catch (Exception exception) {
                    exception.printStackTrace();
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
