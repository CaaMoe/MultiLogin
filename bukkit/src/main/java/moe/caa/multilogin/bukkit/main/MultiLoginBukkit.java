package moe.caa.multilogin.bukkit.main;

import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.bukkit.auth.BukkitAuthCore;
import moe.caa.multilogin.bukkit.auth.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.bukkit.impl.BukkitServer;
import moe.caa.multilogin.bukkit.impl.BukkitUserLogin;
import moe.caa.multilogin.bukkit.loader.impl.BaseBukkitPlugin;
import moe.caa.multilogin.bukkit.loader.main.MultiLoginBukkitLoader;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class MultiLoginBukkit extends BaseBukkitPlugin implements IPlugin {
    @Getter
    private static MultiLoginBukkit instance;
    @Getter
    private final MultiCore core = new MultiCore(this);
    private IServer server;

    public MultiLoginBukkit(MultiLoginBukkitLoader loader, Server server) {
        super(loader, server);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = this;
        server = new BukkitServer(getServer(), this);
        if (!core.init()) setEnabled(false);
    }

    @Override
    public void onDisable() {
        core.disable();
    }

    @Override
    public void initService() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Class<?> craftServerClass = getServer().getClass();
        Method craftServerGetHandle = craftServerClass.getDeclaredMethod("getHandle");
        Class<?> dedicatedPlayerListClass = craftServerGetHandle.getReturnType();
        Method dedicatedPlayerListGetHandler = dedicatedPlayerListClass.getDeclaredMethod("getServer");
        Class<?> minecraftServerClass = dedicatedPlayerListGetHandler.getReturnType().getSuperclass();

        Field field = ReflectUtil.handleAccessible(ReflectUtil.getField(minecraftServerClass, MinecraftSessionService.class), true);
        Object obj = dedicatedPlayerListGetHandler.invoke(craftServerGetHandle.invoke(getServer()));

        HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanServer.getAuthenticationService());
        mlymss.setVanService(vanServer);
        field.set(obj, mlymss);
    }

    @Override
    public void initOther() {
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            private void onLogin(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {
                if (asyncPlayerPreLoginEvent.getUniqueId().equals(BukkitAuthCore.getDIRTY_UUID())) {
                    for (BukkitUserLogin login : BukkitAuthCore.getLoginCachedHashSet().getEntrySet()) {
                        if (login.getUsername().equals(asyncPlayerPreLoginEvent.getName())) {
                            asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, login.getKickMessage() == null ? "喜报\nNMSL" : login.getKickMessage());
                            return;
                        }
                    }
                    asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "喜报\nNMSL");
                }
            }
        }, getThis());

        CommandHandler ch = new CommandHandler(this);
        getThis().getCommand("multilogin").setExecutor(ch);
        getThis().getCommand("multilogin").setTabCompleter(ch);
        getThis().getCommand("whitelist").setExecutor(ch);
        getThis().getCommand("whitelist").setTabCompleter(ch);
    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        Level vanLevel;
        if (level == LoggerLevel.ERROR) vanLevel = Level.SEVERE;
        else if (level == LoggerLevel.WARN) vanLevel = Level.WARNING;
        else if (level == LoggerLevel.INFO) vanLevel = Level.INFO;
        else if (level == LoggerLevel.DEBUG) return;
        else vanLevel = Level.INFO;
        getLogger().log(vanLevel, message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return server;
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }
}
