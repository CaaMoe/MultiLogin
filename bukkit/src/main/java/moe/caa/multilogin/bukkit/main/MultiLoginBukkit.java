/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.main.MultiLoginBukkit
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.main;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.bukkit.auth.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.bukkit.impl.BukkitSchedule;
import moe.caa.multilogin.bukkit.impl.BukkitSender;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.bukkit.support.expansions.MultiLoginPlaceholder;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    public static AbstractScheduler schedule;
    public static Gson authGson;
    public static MultiLoginBukkit plugin;
    private CommandHandler commandHandler;

    @Override
    public void initCoreService() throws Exception {
        Class<?> craftServerClass = getServer().getClass();
        Method craftServerGetHandle = craftServerClass.getDeclaredMethod("getHandle");
        Class<?> dedicatedPlayerListClass = craftServerGetHandle.getReturnType();
        Method dedicatedPlayerListGetHandler = dedicatedPlayerListClass.getDeclaredMethod("getServer");
        Class<?> minecraftServerClass = dedicatedPlayerListGetHandler.getReturnType();

        Field field = ReflectUtil.getField(minecraftServerClass, MinecraftSessionService.class, true);
        Object obj = dedicatedPlayerListGetHandler.invoke(craftServerGetHandle.invoke(getServer()));

        HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanServer.getAuthenticationService());
        mlymss.setVanService(vanServer);
        field.set(obj, mlymss);
    }

    @Override
    public void onEnable() {
        schedule = new BukkitSchedule(this);
        plugin = this;
        setEnabled(MultiCore.init(this));
    }

    @Override
    public void initOtherService() {
        getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        commandHandler = new CommandHandler();
        getCommand("whitelist").setExecutor(this);
        getCommand("whitelist").setTabCompleter(this);
        getCommand("multilogin").setExecutor(this);
        getCommand("multilogin").setTabCompleter(this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MultiLoginPlaceholder(this).register();
                MultiLogger.log(LoggerLevel.INFO, LanguageKeys.BUKKIT_LOADED_PLACEHOLDER.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
                MultiLogger.log(LoggerLevel.ERROR, t);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.BUKKIT_FAIL_LOAD_PLACEHOLDER.getMessage());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandHandler.tabCompete(new BukkitSender(sender), command.getName(), args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        commandHandler.execute(new BukkitSender(sender), command.getName(), args);
        return true;
    }

    @Override
    public void onDisable() {
        MultiCore.disable();
    }

    @Override
    public void shutdown() {
        getServer().shutdown();
    }

    @Override
    public InputStream getJarResource(String path) {
        return getResource(path);
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return getServer().getOnlinePlayers().stream().map(BukkitSender::new).collect(Collectors.toList());
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public AbstractScheduler getSchedule() {
        return schedule;
    }

    @Override
    public boolean isOnlineMode() {
        return getServer().getOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        Player player = getServer().getPlayer(uuid);
        return player == null ? null : new BukkitSender(player);
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) ret.add(new BukkitSender(player));
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return authGson;
    }

    @Override
    public Type authResultType() {
        return HasJoinedMinecraftServerResponse.class;
    }
}
