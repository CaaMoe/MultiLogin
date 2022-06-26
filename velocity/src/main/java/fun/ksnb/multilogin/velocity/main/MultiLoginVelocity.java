package fun.ksnb.multilogin.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import fun.ksnb.multilogin.velocity.injector.MultiInjTask;
import fun.ksnb.multilogin.velocity.logger.Slf4jLoggerBridge;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MultiLoginVelocity implements IPlugin {
    private final Path dataDirectory;
    private final ProxyServer server;
    @Getter
    private final VelocityServer runServer;
    private final PluginLoader pluginLoader;
    private MultiCoreAPI multiCoreAPI;

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws Exception {
        this.server = server;
        this.runServer = new VelocityServer(this.server);
        this.dataDirectory = dataDirectory;
        LoggerProvider.setLogger(new Slf4jLoggerBridge(logger));
        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        try {
            multiCoreAPI = pluginLoader.getCoreObject();
            multiCoreAPI.load();
            new MultiInjTask(multiCoreAPI).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) throws IOException {
        try {
            pluginLoader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            multiCoreAPI = null;
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
