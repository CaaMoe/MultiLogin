package fun.ksnb.multilogin.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import fun.ksnb.multilogin.velocity.logger.Slf4jLoggerBridge;
import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

/**
 * Velocity Main
 */
public class MultiLoginVelocity implements IPlugin {
    private final Path dataDirectory;
    @Getter
    private final com.velocitypowered.proxy.VelocityServer server;
    @Getter
    private final VelocityServer runServer;
    private final PluginLoader pluginLoader;
    @Getter
    private MultiCoreAPI multiCoreAPI;

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {

        this.server = (com.velocitypowered.proxy.VelocityServer) server;
        this.runServer = new VelocityServer(this.server);
        this.dataDirectory = dataDirectory;
        LoggerProvider.setLogger(new Slf4jLoggerBridge(logger));
        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load("MultiLogin-Velocity-Injector.JarFile");
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
            server.shutdown();
            return;
        }
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        try {
            multiCoreAPI = pluginLoader.getCoreObject();
            multiCoreAPI.load();
            Injector injector = (Injector) pluginLoader.findClass("moe.caa.multilogin.velocity.injector.VelocityInjector").getConstructor().newInstance();
            injector.inject(multiCoreAPI);
        } catch (Throwable e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            server.shutdown();
            return;
        }
        new GlobalListener(this).register();
        new CommandHandler(this).register("multilogin");
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        try {
            multiCoreAPI.close();
            pluginLoader.close();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
        } finally {
            multiCoreAPI = null;
            server.shutdown();
        }
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public File getTempFolder() {
        return new File(getDataFolder(), "tmp");
    }
}
