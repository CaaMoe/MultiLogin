/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.impl.MultiLoginBukkit
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.impl;

import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.bukkit.Metrics;
import moe.caa.multilogin.bukkit.expansions.MultiLoginPlaceholderExpansion;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.bukkit.yggdrasil.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.command.CommandMain;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.util.I18n;
import moe.caa.multilogin.core.util.ReflectUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MultiLogin - Bukkit
 */
public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    public static final Map<UUID, Long> LOGIN_CACHE = new Hashtable<>();
    public static final Map<UUID, UserEntry> USER_CACHE = new Hashtable<>();

    /**
     * 反射替换原版的验证服务
     */
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
//        AuthTask.setServicePair located at MultiLoginYggdrasilMinecraftSessionService.setVanService
        if (!getServer().getOnlineMode()) {
            getLogger().severe(I18n.getTransString("bukkit_error_loading_online"));
            setEnabled(false);
            return;
        }

        try {
            initCoreService();
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().severe(I18n.getTransString("plugin_error_loading_reflect"));
            setEnabled(false);
            return;
        }

        new Metrics(this, 9889);

        getServer().getPluginManager().registerEvents(new BukkitListener(), this);

        getCommand("whitelist").setExecutor((sender, cmd, l, strings) -> CommandMain.submitCommand("whitelist", new BukkitSender(sender), strings));
        getCommand("multilogin").setExecutor((sender, cmd, l, strings) -> CommandMain.submitCommand("multilogin", new BukkitSender(sender), strings));
        getCommand("whitelist").setTabCompleter((sender, cmd, l, strings) -> CommandMain.suggestCommand("whitelist", new BukkitSender(sender), strings));
        getCommand("multilogin").setTabCompleter((sender, cmd, l, strings) -> CommandMain.suggestCommand("multilogin", new BukkitSender(sender), strings));

        if (!MultiCore.initService(this)) {
            setEnabled(false);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiLoginPlaceholderExpansion().register();
            getLogger().info(I18n.getTransString("bukkit_loaded_papi"));
        }
        getLogger().info(I18n.getTransString("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        try {
            PluginData.close();
        } catch (Throwable ignored) {
        }
        getLogger().info(I18n.getTransString("plugin_disable"));
        getServer().getScheduler().cancelTasks(this);
        getServer().shutdown();
    }

    @Override
    public File getPluginDataFolder() {
        return getDataFolder();
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
        });
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
    public void runTask(Runnable run) {
        getServer().getScheduler().runTask(this, run);
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
