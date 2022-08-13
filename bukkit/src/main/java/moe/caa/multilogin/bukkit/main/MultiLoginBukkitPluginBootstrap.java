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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
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
    public void initService() throws Exception {
        try {
            Class<?> serviceClass = Class.forName("net.minecraft.server.Services");

            final Object[] minecraftServerClassMO = forceGet(vanServer, serviceClass, new HashSet<>());

            Field serviceField = ReflectUtil.handleAccessible((Field) minecraftServerClassMO[1], true);
            Object serverObj = minecraftServerClassMO[0];

            final Object serviceObj = serviceField.get(serverObj);

            LinkedHashMap<Field, Object> fieldObjectMap = new LinkedHashMap<>();
            MultiLoginYggdrasilMinecraftSessionService sessionService = null;

            for (Field field : serviceObj.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(serviceObj);
                if(value instanceof MinecraftSessionService){
                    sessionService = new MultiLoginYggdrasilMinecraftSessionService(((HttpMinecraftSessionService) value).getAuthenticationService());
                    sessionService.setVanService((HttpMinecraftSessionService) value);
                    sessionService.setBootstrap(this);
                    value = sessionService;
                }
                fieldObjectMap.put(field, value);
            }
            if(sessionService == null) {
                throw new RuntimeException("Unsupported Server.");
            }

            final Constructor<?> declaredConstructor = serviceObj.getClass().getDeclaredConstructor(
                    fieldObjectMap.keySet().stream().map(Field::getType).toArray(Class[]::new)
            );

            final Object o = declaredConstructor.newInstance(fieldObjectMap.values().toArray());

            serviceField.set(serverObj, o);
            return;
        } catch (ClassNotFoundException ignored) {

        }
        final Object[] objects = forceGet(vanServer, MinecraftSessionService.class, new HashSet<>());
        Field field = ReflectUtil.handleAccessible((Field) objects[1], true);
        Object obj = objects[0];

        HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanServer.getAuthenticationService());
        mlymss.setVanService(vanServer);
        mlymss.setBootstrap(this);
        field.set(obj, mlymss);
    }

    private Object[] forceGet(Object source, Type needGet, Set<Type> ignore) throws ClassNotFoundException {
        Class<?> sourceClass = source.getClass();
        // 双重遍历确保能获取到本类和父类所有的Field
        do {
            for (Field declaredField : sourceClass.getDeclaredFields()) {
                try {
                    // 类型匹配，返回Field所在的类的实例和Field
                    if (declaredField.getType() == needGet) {
                        if (sourceClass.getName().startsWith("net.minecraft.")) {
                            return new Object[]{source, declaredField};
                        }
                    }
                    declaredField.setAccessible(true);
                    final Object o = declaredField.get(source);
                    if (ignore.add(o.getClass())) return forceGet(o, needGet, ignore);

                } catch (Throwable ignored) {
                }
            }
        } while ((sourceClass = sourceClass.getSuperclass()) != null);

        throw new ClassNotFoundException(needGet.getTypeName());
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
