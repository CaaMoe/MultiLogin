package fun.ksnb.multilogin.velocity.loader.main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;

public class MultiLoginVelocityLoader implements IPluginLoader {
    @Getter
    private final ProxyServer server;
    private final Logger logger;
    @Getter
    private final Path dataDirectory;
    private MultiLoginCoreLoader coreLoader;
    private BasePluginBootstrap pluginBootstrap;

    @Inject
    public MultiLoginVelocityLoader(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        try {
            coreLoader = new MultiLoginCoreLoader(this);
            if (!coreLoader.start()) {
                shutdown();
                return;
            }
            pluginBootstrap = coreLoader.loadBootstrap(
                    "fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap",
                    new Class[]{MultiLoginVelocityLoader.class}, new Object[]{this});
            pluginBootstrap.onLoad();
        } catch (Throwable throwable) {
            loggerLog(Level.SEVERE, "A FATAL ERROR OCCURRED DURING INITIALIZATION.", throwable);
            onDisable(null);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getSectionJarFileName() {
        return "MultiLogin-Velocity.JarFile";
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        if (level == Level.SEVERE) getLogger().error(message, throwable);
        else if (level == Level.WARNING) getLogger().warn(message, throwable);
        else if (level == Level.INFO) getLogger().info(message, throwable);
        else getLogger().info(message, throwable);
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        if (pluginBootstrap != null) {
            pluginBootstrap.onLoad();
            pluginBootstrap.onEnable();
        }
    }

    public void disable() {
        if (pluginBootstrap != null) pluginBootstrap.onDisable();
        pluginBootstrap = null;
        coreLoader.close();
        shutdown();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        disable();
    }
}
