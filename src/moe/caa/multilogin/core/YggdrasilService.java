package moe.caa.multilogin.core;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

public class YggdrasilService {
    public static YggdrasilService OFFICIAL;

    private final String path;
    private final String name;
    private final String url;
    private final PluginData.ConvUuid convUuid;
    private final boolean whitelist;

    public YggdrasilService(String path, String name, String url, PluginData.ConvUuid convUuid, boolean whitelist, boolean logger) {
        this.name = name;
        this.url = url;
        this.convUuid = convUuid;
        this.path = path;
        this.whitelist = whitelist;
        if(logger){
            Logger log = MultiCore.getPlugin().getMLPluginLogger();
            log.info(String.format("添加Yggdrasil验证服务器: %s, URL: %s", name, url));
        }

    }

    public static YggdrasilService fromYaml(String path, IConfiguration section){
        if (section != null){
            String name = section.getString("name");
            String url = section.getString("url");
            String convUuid = section.getString("convUuid");
            PluginData.ConvUuid convUuidEnum = null;
            boolean whitelist = true;
            try {
                convUuidEnum = PluginData.ConvUuid.valueOf(convUuid);
                whitelist = section.getBoolean("whitelist");
            } catch (Exception ignore){
            }
            if(!PluginData.isEmpty(name) && !PluginData.isEmpty(url) && convUuidEnum != null){
                return new YggdrasilService(path, name, url, convUuidEnum,whitelist, true);
            }
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public PluginData.ConvUuid getConvUuid() {
        return convUuid;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean checkUrl(){
        try {
            URL url = new URL(this.url);
            return url.openConnection().getInputStream().available() > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    public URL buildUrl(String arg) throws MalformedURLException {
        return new URL(url + "/sessionserver/session/minecraft/" + arg);
    }

    public String buildUrlStr(String arg) throws MalformedURLException {
        return url + "/sessionserver/session/minecraft/" + arg;
    }

    public static<T> AuthResult<T> yggAuth(String arg, Gson gson, Class<T> clazz)  {
        T getResult = null;
        boolean down = false;
        Map<Future<T>, YggdrasilService> tasks = new HashMap<>();
        if(PluginData.isOfficialYgg()){
            FutureTask<T> task = new FutureTask<T>(()->{
                String result = MultiCore.httpGet(String.format("https://sessionserver.mojang.com/session/minecraft/%s", arg));
                return gson.fromJson(result, clazz);
            });
            MultiCore.getPlugin().runTaskAsyncLater(task, 0);
            tasks.put(task, YggdrasilService.OFFICIAL);
        }

        for(YggdrasilService section : PluginData.getServiceSet()){
            FutureTask<T> task = new FutureTask<>(() -> {
                String result = MultiCore.httpGet(section.buildUrlStr(arg));
                return gson.fromJson(result, clazz);
            });
            MultiCore.getPlugin().runTaskAsyncLater(task, 0);
            tasks.put(task, YggdrasilService.OFFICIAL);
        }

        Future<T> taskDown = null;
        long time = System.currentTimeMillis() + PluginData.getTimeOut();
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
                return new AuthResult<>(AuthErrorEnum.SERVER_DOWN, null, null);
            return new AuthResult<>(AuthErrorEnum.VALIDATION_FAILED, null, null);
        }
        return new AuthResult<T>(null, getResult, tasks.get(taskDown));
    }

    public static class AuthResult<T> {
        private final AuthErrorEnum err;
        private final T result;
        private final YggdrasilService yggdrasilService;

        private AuthResult(AuthErrorEnum err, T result, YggdrasilService yggdrasilService) {
            this.err = err;
            this.result = result;
            this.yggdrasilService = yggdrasilService;
        }

        public AuthErrorEnum getErr() {
            return err;
        }

        public T getResult() {
            return result;
        }

        public YggdrasilService getYggdrasilService() {
            return yggdrasilService;
        }
    }

    public enum AuthErrorEnum{
        SERVER_DOWN,
        VALIDATION_FAILED;
    }

}
