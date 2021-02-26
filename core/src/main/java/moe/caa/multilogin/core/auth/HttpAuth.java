package moe.caa.multilogin.core.auth;

import com.google.gson.Gson;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.PluginData;
import moe.caa.multilogin.core.data.YggdrasilServiceEntry;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class HttpAuth {
    public static<T> AuthResult<T> yggAuth(String name, String arg, Gson gson, Class<T> clazz) throws SQLException {
        List<List<YggdrasilServiceEntry>> order = MultiCore.getVeriOrder(name);
        boolean down = false;
        for (List<YggdrasilServiceEntry> entries : order) {
            AuthResult<T> result = yggAuth(entries, arg, gson, clazz);
            if(result.getErr() == null){
                return result;
            }
            if (result.getErr() == AuthErrorEnum.SERVER_DOWN) {
                down = true;
            }
        }
        return new AuthResult<>(down ? AuthErrorEnum.SERVER_DOWN : AuthErrorEnum.VALIDATION_FAILED);
    }

    private static<T> AuthResult<T> yggAuth(List<YggdrasilServiceEntry> serviceEntryList, String arg, Gson gson, Class<T> clazz){
        T getResult = null;
        boolean down = false;
        Map<Future<T>, YggdrasilServiceEntry> tasks = new HashMap<>();
        Future<T> taskDown = null;
        long time = System.currentTimeMillis() + PluginData.getTimeOut();

        for(YggdrasilServiceEntry entry : serviceEntryList){
            FutureTask<T> task = new FutureTask<>(() -> gson.fromJson(MultiCore.httpGet(entry.buildUrlStr(arg)), clazz));
            MultiCore.getPlugin().runTaskAsyncLater(task, 0);
            tasks.put(task, entry);
        }

        dos:while(time > System.currentTimeMillis() && tasks.size() != 0){
            Iterator<Future<T>> itr = tasks.keySet().iterator();
            while (itr.hasNext()){
                Future<T> task = itr.next();
                if(task.isDone()){
                    try {
                        getResult = task.get();
                    } catch (Exception ignored) {
                        down = true;
                    }
                    if(getResult != null){
                        taskDown = task;
                        break dos;
                    }
                    itr.remove();
                }
            }
        }
        for (Future<T> future : tasks.keySet()){
            future.cancel(true);
        }
        if(getResult == null){
            if(down)
                return new AuthResult<>(AuthErrorEnum.SERVER_DOWN);
            return new AuthResult<>(AuthErrorEnum.VALIDATION_FAILED);
        }
        return new AuthResult<T>(getResult, tasks.get(taskDown));
    }
}
