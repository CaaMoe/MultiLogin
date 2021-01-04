package moe.caa.multilogin.core;

import com.google.gson.*;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MultiCore {
    private static IPlugin plugin;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String preV = null;
    private static String relV = null;
    private static final BASE64Decoder decoder = new BASE64Decoder();
    private static final boolean CURRENT_PRE_VERSION = true;

    public static IConfiguration getConfig(){
        return plugin.getPluginConfig();
    }

    public static IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException {
        return plugin.yamlLoadConfiguration(reader);
    }

    public static InputStream getResource(String path){
        return plugin.getPluginResource(path);
    }

    public static void kickPlayer(UUID uuid, String msg) {
        plugin.kickPlayer(uuid, msg);
    }

    public static IPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(IPlugin plugin) {
        MultiCore.plugin = plugin;
        plugin.runTaskAsyncTimer(MultiCore::update, 0, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(MultiCore::setUpUpdate, 0);
        plugin.runTaskAsyncTimer(MultiCore::save, 0, 20 * 60);
        try {
            PluginData.reloadConfig();
            PluginData.readData();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getMLPluginLogger().severe("无法读取配置或数据文件，请检查！");
            plugin.setPluginEnabled(false);
        }
    }

    public static void save(){
        try {
            PluginData.saveData();
        } catch (IOException e) {
            plugin.getMLPluginLogger().severe("无法保存数据文件");
            e.printStackTrace();
        }
    }

    public static boolean isUpdate(){
        if(preV == null && relV == null){
            return false;
        }
        if(preV == null && CURRENT_PRE_VERSION){
            return true;
        }
        if(preV != null && CURRENT_PRE_VERSION && !preV.endsWith(getCurrentV())){
            return true;
        }
        if(relV != null && !CURRENT_PRE_VERSION && !relV.endsWith(getCurrentV())){
            return true;
        }
        return false;
    }

    public static String getPreV() {
        return preV;
    }

    public static String getRelV() {
        return relV;
    }

    public static String getCurrentV(){
        return plugin.getVersion();
    }


    public static void setUpUpdate(){
        update();
        if (isUpdate()) {
            plugin.getMLPluginLogger().info("插件有新的版本发布");
            plugin.getMLPluginLogger().info(String.format("当前版本为 %s", getCurrentV()));
            plugin.getMLPluginLogger().info(String.format("最新预发布版本为 %s", preV));
            plugin.getMLPluginLogger().info(String.format("最新发布版本为 %s", relV));
        }
    }

    private static void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/version.json?ref=master");
            JsonObject jo = (JsonObject) new JsonParser().parse(httpGet(url));
            String v = new String(decoder.decodeBuffer(jo.get("content").getAsString()));
            JsonObject content = (JsonObject) new JsonParser().parse(v);
            Set<Map.Entry<String, JsonElement>> set = content.entrySet();
            String preV = null;
            String relV = null;
            for (Map.Entry<String, JsonElement> entry : set) {
                if(entry.getKey().equalsIgnoreCase("pre-release")){
                    preV = entry.getValue().getAsString();
                } else  if(entry.getKey().equalsIgnoreCase("release")){
                    relV = entry.getValue().getAsString();
                }
            }
            MultiCore.preV = preV;
            MultiCore.relV = relV;
        } catch (Exception ignore){}
    }

    public static<T> AuthResult<T> yggAuth(String arg, Gson gson, Class<T> clazz) throws ExecutionException, InterruptedException {
        T getResult = null;
        boolean down = false;
        Map<Future<T>, YggdrasilServiceSection> tasks = new HashMap<>();
        if(PluginData.isOfficialYgg()){
            FutureTask<T> task = new FutureTask<T>(()->{
                String result = httpGet(String.format("https://sessionserver.mojang.com/session/minecraft/%s", arg));
                return gson.fromJson(result, clazz);
            });
            plugin.runTaskAsyncLater(task, 0);
            tasks.put(task, YggdrasilServiceSection.OFFICIAL);
        }

        for(YggdrasilServiceSection section : PluginData.getServiceSet()){
            FutureTask<T> task = new FutureTask<>(() -> {
                String result = httpGet(section.buildUrlStr(arg));
                return gson.fromJson(result, clazz);
            });
            plugin.runTaskAsyncLater(task, 0);
            tasks.put(task, YggdrasilServiceSection.OFFICIAL);
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
                return new AuthResult<>("SERVICE DOWN", null, null);
            return new AuthResult<>("VALIDATION FAILED", null, null);
        }
        return new AuthResult<T>(null, taskDown.get(), tasks.get(taskDown));
    }

    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
    }

    public static String httpGet(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static class AuthResult<T> {
        private final String err;
        private final T result;
        private final YggdrasilServiceSection yggdrasilService;

        AuthResult(String err, T result, YggdrasilServiceSection yggdrasilService) {
            this.err = err;
            this.result = result;
            this.yggdrasilService = yggdrasilService;
        }

        public String getErr() {
            return err;
        }

        public T getResult() {
            return result;
        }

        public YggdrasilServiceSection getYggdrasilService() {
            return yggdrasilService;
        }
    }
}
