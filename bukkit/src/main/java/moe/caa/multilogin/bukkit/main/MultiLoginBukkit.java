package moe.caa.multilogin.bukkit.main;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.logger.bridges.JavaLoggerBridge;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.bukkit.impl.BukkitServer;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    @Getter
    private static MultiLoginBukkit instance;
    @Getter
    private BukkitServer runServer;
    private PluginLoader pluginLoader;
    @Getter
    private MultiCoreAPI multiCoreAPI;

    @Override
    public void onLoad() {
        instance = this;
        LoggerProvider.setLogger(new JavaLoggerBridge(getLogger()));
        runServer = new BukkitServer(this);
        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
            runServer.shutdown();
            return;
        }
    }

    @Override
    public void onEnable() {
        try {
            multiCoreAPI = pluginLoader.getCoreObject();
            multiCoreAPI.load();
//            Injector injector = (Injector) pluginLoader.findClass("moe.caa.multilogin.bungee.injector.BungeeInjector").getConstructor().newInstance();
//            injector.inject(multiCoreAPI);
        } catch (Throwable e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            runServer.shutdown();
            return;
        }

        new GlobalListener(this).register();
        new CommandHandler(this).register();
    }

    @Override
    public void onDisable() {
        try {
            multiCoreAPI.close();
            pluginLoader.close();
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
