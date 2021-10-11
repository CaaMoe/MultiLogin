package moe.caa.multilogin.bukkit.main;

import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.bukkit.auth.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.bukkit.impl.BukkitServer;
import moe.caa.multilogin.bukkit.listener.BukkitListener;
import moe.caa.multilogin.bukkit.support.expansions.MultiLoginExpansion;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class MultiLoginBukkitPluginBootstrap extends BasePluginBootstrap implements IPlugin {
    @Getter
    private static MultiLoginBukkitPluginBootstrap instance;

    @Getter
    private final MultiCore core;

    private final JavaPlugin vanPlugin;
    private final Server vanServer;
    private final BukkitListener listener;
    private IServer server;

    public MultiLoginBukkitPluginBootstrap(JavaPlugin vanPlugin, Server server) {
        this.vanServer = server;
        this.vanPlugin = vanPlugin;
        this.core = new MultiCore(this);

        listener = new BukkitListener(this);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = this;
        server = new BukkitServer(vanServer, vanPlugin);
        if (!core.init()) onDisable();
    }

    @Override
    public void onDisable() {
        core.disable();
    }

    @Override
    public void initService() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Class<?> craftServerClass = vanServer.getClass();
        Method craftServerGetHandle = craftServerClass.getDeclaredMethod("getHandle");
        Class<?> dedicatedPlayerListClass = craftServerGetHandle.getReturnType();
        Method dedicatedPlayerListGetHandler = dedicatedPlayerListClass.getDeclaredMethod("getServer");
        Class<?> minecraftServerClass = dedicatedPlayerListGetHandler.getReturnType().getSuperclass();

        Field field = ReflectUtil.handleAccessible(ReflectUtil.getField(minecraftServerClass, MinecraftSessionService.class), true);
        Object obj = dedicatedPlayerListGetHandler.invoke(craftServerGetHandle.invoke(vanServer));

        HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanServer.getAuthenticationService());
        mlymss.setVanService(vanServer);
        field.set(obj, mlymss);
    }

    @Override
    public void initOther() {
        vanServer.getPluginManager().registerEvents(listener, vanPlugin);
        CommandHandler ch = new CommandHandler(this);
        vanPlugin.getCommand("multilogin").setExecutor(ch);
        vanPlugin.getCommand("multilogin").setTabCompleter(ch);
        vanPlugin.getCommand("whitelist").setExecutor(ch);
        vanPlugin.getCommand("whitelist").setTabCompleter(ch);

        if (vanServer.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MultiLoginExpansion(this).register();
            } catch (Throwable t) {
                core.getLogger().log(LoggerLevel.ERROR, "An error occurred on the registered PlaceholderAPI variable.", t);
            }
        }
    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        Level vanLevel;
        if (level == LoggerLevel.ERROR) vanLevel = Level.SEVERE;
        else if (level == LoggerLevel.WARN) vanLevel = Level.WARNING;
        else if (level == LoggerLevel.INFO) vanLevel = Level.INFO;
        else if (level == LoggerLevel.DEBUG) return;
        else vanLevel = Level.INFO;
        vanPlugin.getLogger().log(vanLevel, message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return server;
    }

    @Override
    public File getDataFolder() {
        return vanPlugin.getDataFolder();
    }

    @Override
    public String getPluginVersion() {
        return vanPlugin.getDescription().getVersion();
    }

    public PluginDescriptionFile getDescriptionFile() {
        return vanPlugin.getDescription();
    }
}
