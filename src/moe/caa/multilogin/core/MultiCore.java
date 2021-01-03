package moe.caa.multilogin.core;

import com.google.gson.*;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
            JsonObject jo = (JsonObject) new JsonParser().parse(new InputStreamReader(url.openConnection().getInputStream()));
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
}
