package fun.ksnb.multilogin.bungee.main;

import fun.ksnb.multilogin.bungee.impl.BungeeServer;
import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.logger.bridges.JavaLoggerBridge;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.loader.main.PluginLoader;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

/**
 * Bungee Main
 */
public class MultiLoginBungee extends Plugin implements IPlugin {
    @Getter
    private BungeeServer runServer;
    private PluginLoader pluginLoader;
    @Getter
    private MultiCoreAPI multiCoreAPI;

    @Override
    public void onLoad() {
        LoggerProvider.setLogger(new JavaLoggerBridge(getLogger()));
        this.runServer = new BungeeServer(getProxy());
        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load("MultiLogin-Bungee-Injector.JarFile");
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
            Injector injector = (Injector) pluginLoader.findClass("moe.caa.multilogin.bungee.injector.BungeeInjector").getConstructor().newInstance();
            injector.inject(multiCoreAPI);
        } catch (Throwable e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            runServer.shutdown();
            return;
        }
        new GlobalListener(this).register();
        new CommandHandler(this).register("multilogin");
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
