package moe.caa.multilogin.bukkit.main;

import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.logger.bridges.JavaLoggerBridge;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.bukkit.impl.BukkitServer;
import moe.caa.multilogin.bukkit.support.expansions.MultiLoginPlaceholderExpansion;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Bukkit Main
 */
public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    @Getter
    private BukkitServer runServer;
    @Getter
    private PluginLoader mlPluginLoader;
    @Getter
    private MultiCoreAPI multiCoreAPI;
    private ClassLoader mlClassLoader;

    @Override
    public void onLoad() {
        LoggerProvider.setLogger(new JavaLoggerBridge(getLogger()));
        this.runServer = new BukkitServer(this, getServer());
        this.mlPluginLoader = new PluginLoader(this);
        try {
            mlPluginLoader.load("MultiLogin-Bukkit-Injector.JarFile");
            mlClassLoader = mlPluginLoader.getPluginClassLoader().self();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
            runServer.shutdown();
            return;
        }
    }

    @Override
    public void onEnable() {
        try {
            multiCoreAPI = mlPluginLoader.getCoreObject();
            multiCoreAPI.load();
            Injector injector = (Injector) mlPluginLoader.findClass("moe.caa.multilogin.bukkit.injector.BukkitInjector").getConstructor().newInstance();
            injector.inject(multiCoreAPI);
        } catch (Throwable e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            runServer.shutdown();
            return;
        }
        new GlobalListener(this).register();
        new CommandHandler(this).register("multilogin");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new MultiLoginPlaceholderExpansion(this).register();
            } catch (Throwable throwable) {
                LoggerProvider.getLogger().error("Placeholder registration failed.");
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            multiCoreAPI.close();
            mlPluginLoader.close();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
        } finally {
            multiCoreAPI = null;
            runServer.shutdown();
        }
    }

    @Override
    public File getTempFolder() {
        return new File(getDataFolder(), "tmp");
    }
}
