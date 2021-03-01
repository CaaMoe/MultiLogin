package moe.caa.multilogin.bukkit.impl;

import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.bukkit.Metrics;
import moe.caa.multilogin.bukkit.expansions.MultiLoginPlaceholderExpansion;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.bukkit.yggdrasil.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.IPlugin;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.util.ReflectUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    public static final Map<UUID, Long> LOGIN_CACHE = new Hashtable<>();
    public static final Map<UUID, UserEntry> USER_CACHE = new Hashtable<>();

    private void initCoreService() throws Exception {
        final String NMS_VERSION = getServer().getClass().getPackage().getName().split("\\.")[3];
        final Class<?> CRAFT_SERVER_CLASS = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".CraftServer");
        final Method CRAFT_SERVER_GET_HANDLE = CRAFT_SERVER_CLASS.getDeclaredMethod("getHandle");
        final Class<?> DEDICATED_PLAYER_LIST_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".DedicatedPlayerList");
        final Method DEDICATED_PLAYER_LIST_GET_HANDLE = DEDICATED_PLAYER_LIST_CLASS.getDeclaredMethod("getServer");
        final Class<?> MINECRAFT_SERVER_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".MinecraftServer");
        final Field field = ReflectUtil.getField(MINECRAFT_SERVER_CLASS, MinecraftSessionService.class);
        final Object obj = DEDICATED_PLAYER_LIST_GET_HANDLE.invoke(CRAFT_SERVER_GET_HANDLE.invoke(getServer()));

        final HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);

        final MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanServer.getAuthenticationService());
        mlymss.setVanService(vanServer);

        field.set(obj, mlymss);
    }

    @Override
    public void onEnable() {
        if (!getServer().getOnlineMode()) {
            getLogger().severe("插件只能运行在“online-mode=true”的环境下");
            getLogger().severe("请打开服务端的正版验证！");
            setEnabled(false);
            return;
        }

        try {
            initCoreService();
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().severe("初始化修改失败，插件可能不兼容您的服务端！");
            setEnabled(false);
            return;
        }

        new Metrics(this, 9889);

        getServer().getPluginManager().registerEvents(new BukkitListener(), this);

        getCommand("whitelist").setExecutor((sender, cmd, l, strings) -> MultiCore.submitCommand("whitelist", new BukkitSender(sender), strings));
        getCommand("multilogin").setExecutor((sender, cmd, l, strings) -> MultiCore.submitCommand("multilogin", new BukkitSender(sender), strings));
        getCommand("whitelist").setTabCompleter((sender, cmd, l, strings) -> MultiCore.suggestCommand("whitelist", new BukkitSender(sender), strings));
        getCommand("multilogin").setTabCompleter((sender, cmd, l, strings) -> MultiCore.suggestCommand("multilogin", new BukkitSender(sender), strings));

        if (!MultiCore.initService(this)) {
            setEnabled(false);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiLoginPlaceholderExpansion().register();
            getLogger().info("已加载PlaceholderAPI");
        }
        getLogger().info("插件已加载");
    }

    @Override
    public void onDisable() {
        try {
            PluginData.close();
        } catch (Throwable ignored) {
        }
        getLogger().info("插件已关闭");
        getServer().getScheduler().cancelTasks(this);
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
    public IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException {
        return new BukkitConfiguration(YamlConfiguration.loadConfiguration(reader));
    }

    @Override
    public InputStream getPluginResource(String path) {
        return getResource(path);
    }

    @Override
    public void kickPlayer(UUID uuid, String msg) {
        MultiCore.getPlugin().runTask(() -> {
            Player p = getServer().getPlayer(uuid);
            if (p != null) {
                p.kickPlayer(msg);
            }
        }, 0);
    }

    @Override
    public Logger getPluginLogger() {
        return getLogger();
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
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
    public void runTask(Runnable run, long delay) {
        getServer().getScheduler().runTaskLater(this, run, delay);
    }

    @Override
    public Map<UUID, String> getOnlineList() {
        Map<UUID, String> ret = new HashMap<>();
        for (Player player : getServer().getOnlinePlayers()) {
            ret.put(player.getUniqueId(), player.getName());
        }
        return ret;
    }
}
