package moe.caa.bukkit.multilogin;

import com.google.gson.*;
import io.netty.handler.codec.base64.Base64Decoder;
import moe.caa.bukkit.multilogin.listener.BukkitListener;
import org.bukkit.plugin.java.JavaPlugin;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

public final class MultiLogin extends JavaPlugin{
    public static MultiLogin INSTANCE;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BASE64Decoder decoder = new BASE64Decoder();
    private final boolean CURRENT_PRE_VERSION = true;
    private String preV = null;
    private String relV = null;

    @Override
    public void onEnable() {
        MultiLogin.INSTANCE = this;
        if(!getServer().getOnlineMode()){
            getLogger().severe("插件只能运行在“online-mode=true”的环境下");
            getLogger().severe("请打开服务端的正版验证！");
            setEnabled(false);
            return;
        }

        try {
            PluginData.reloadConfig();
            PluginData.readData();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("无法读取配置或数据文件，请检查！");
            setEnabled(false);
            return;
        }

        try {
            NMSUtil.initService(this);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("初始化修改失败，插件可能不兼容您的服务端！");
            setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        WhitelistCommand command = new WhitelistCommand();
        getCommand("whitelist").setExecutor(command);
        getCommand("whitelist").setTabCompleter(command);

        MultiLoginCommand command1 = new MultiLoginCommand();
        getCommand("multilogin").setTabCompleter(command1);
        getCommand("multilogin").setExecutor(command1);

        getServer().getScheduler().runTaskTimerAsynchronously(this, this::save, 0, 20 * 60);
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::update, 0, 20 * 60 * 60 * 24);
        getServer().getScheduler().runTaskLaterAsynchronously(this, this::setUpUpdate, 0);

        getLogger().info("插件已加载");
    }

    public void setUpUpdate(){
        update();
        if (isUpdate()) {
            getLogger().info("插件有新的版本发布");
            getLogger().info(String.format("当前版本为 %s", getCurrentV()));
            getLogger().info(String.format("最新预发布版本为 %s", preV));
            getLogger().info(String.format("最新发布版本为 %s", relV));
        }
    }

    @Override
    public void onDisable() {
        save();
        getServer().shutdown();
    }

    private void save(){
        try {
            PluginData.saveData();
        } catch (IOException e) {
            getLogger().severe("无法保存数据文件");
            e.printStackTrace();
        }
    }

    public boolean isUpdate(){
        if(preV == null && CURRENT_PRE_VERSION){
            return true;
        }
        if(preV != null && CURRENT_PRE_VERSION && !preV.equalsIgnoreCase(getCurrentV())){
            return true;
        }
        if(relV != null && !CURRENT_PRE_VERSION && !relV.equalsIgnoreCase(getCurrentV())){
            return true;
        }
        return false;
    }

    public String getPreV() {
        return preV;
    }

    public String getRelV() {
        return relV;
    }

    public String getCurrentV(){
        return getDescription().getVersion();
    }

    private void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLoginBukkit/contents/version.json?ref=master");
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
            this.preV = preV;
            this.relV = relV;
        } catch (Exception ignore){}
    }

    public static void main(String[] args) {

    }
}
