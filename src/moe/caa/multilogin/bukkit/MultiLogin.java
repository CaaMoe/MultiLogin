package moe.caa.multilogin.bukkit;

import com.google.gson.Gson;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.IPlugin;
import moe.caa.multilogin.core.MultiCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Logger;

public final class MultiLogin extends JavaPlugin implements IPlugin {
    public static MultiLogin INSTANCE;

    @Override
    public void onEnable() {
        MultiLogin.INSTANCE = this;
        MultiCore.setPlugin(this);
        if(!getServer().getOnlineMode()){
            getLogger().severe("插件只能运行在“online-mode=true”的环境下");
            getLogger().severe("请打开服务端的正版验证！");
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

        getCommand("whitelist").setExecutor((sender, cmd, l, strings)->{
            MultiCore.submitCommand("whitelist", new BukkitSender(sender), strings);
            return true;
        });
        getCommand("multilogin").setExecutor((sender, cmd, l, strings)->{
            MultiCore.submitCommand("multilogin", new BukkitSender(sender), strings);
            return true;
        });
        getCommand("whitelist").setTabCompleter((sender, cmd, l, strings)-> MultiCore.suggestCommand("whitelist", new BukkitSender(sender), strings));
        getCommand("multilogin").setTabCompleter((sender, cmd, l, strings)-> MultiCore.suggestCommand("multilogin", new BukkitSender(sender), strings));
        new Metrics(this, 9889);
        getLogger().info("插件已加载");
    }

    @Override
    public void onDisable() {
        MultiCore.save();
        getServer().shutdown();
    }

    @Override
    public File getPluginDataFolder() {
        return getDataFolder();
    }

    @Override
    public IConfiguration getPluginConfig() {
        return new BukkitConfiguration(getConfig());
    }

    @Override
    public void savePluginDefaultConfig() {
        saveDefaultConfig();
    }

    @Override
    public void reloadPluginConfig() {
        reloadConfig();
    }

    @Override
    public IConfiguration yamlLoadConfiguration(InputStreamReader reader) {
        return new BukkitConfiguration(YamlConfiguration.loadConfiguration(reader));
    }

    @Override
    public InputStream getPluginResource(String path) {
        return getResource(path);
    }

    @Override
    public void kickPlayer(UUID uuid, String msg) {
        Player p = Bukkit.getPlayer(uuid);
        if(p != null){
            p.kickPlayer(msg);
        }
    }

    @Override
    public Logger getMLPluginLogger() {
        return getLogger();
    }

    @Override
    public void runTaskAsyncLater(Runnable run, long delay) {
        getServer().getScheduler().runTaskLaterAsynchronously(this, run, delay);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long per) {
        getServer().getScheduler().runTaskTimerAsynchronously(this, run, delay, per);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void setPluginEnabled(boolean b) {
        setEnabled(b);
    }

    @Override
    public Gson getAuthGson() {
        return NMSUtil.getAuthGson();
    }
}
