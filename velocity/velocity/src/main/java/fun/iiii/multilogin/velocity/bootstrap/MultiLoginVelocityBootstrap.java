package fun.iiii.multilogin.velocity.bootstrap;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.api.exception.BreakSignalException;
import moe.caa.multilogin.api.logger.Level;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.schedule.IScheduler;
import moe.caa.multilogin.loader.api.IBootstrap;
import moe.caa.multilogin.loader.api.IPlatformCore;
import moe.caa.multilogin.loader.main.PluginLoader;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class MultiLoginVelocityBootstrap implements IBootstrap {
    public final ProxyServer proxyServer;
    public final File dataFolder;
    public final File tempFolder;
    public final IScheduler scheduler;
    public final PluginLoader pluginLoader;

    public IPlatformCore<MultiLoginVelocityBootstrap> platformCore;

    @Inject
    public MultiLoginVelocityBootstrap(
            ProxyServer server, Logger logger, @DataDirectory Path dataDirectory
    ) {
        this.proxyServer = server;
        this.dataFolder = dataDirectory.toFile();
        this.tempFolder = new File(dataFolder, "tmp");

        this.scheduler = IScheduler.buildSimple();
        this.pluginLoader = new PluginLoader(this);

        setupLogger(logger);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        try {
            // todo
//            pluginLoader.addPlatformLibrary();
            pluginLoader.enable();
        } catch (Throwable e) {
            if (e instanceof BreakSignalException) {
                if (e.getMessage() != null) {
                    LoggerProvider.logger.error(e.getMessage());
                }
            } else {
                LoggerProvider.logger.error("An exception was encountered while loading the plugin.", e);
            }
            proxyServer.shutdown();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        try {
            if (platformCore != null) {
                platformCore.disable();
            }
            pluginLoader.disable();
        } catch (Throwable e) {
            if (e instanceof BreakSignalException) {
                if (e.getMessage() != null) {
                    LoggerProvider.logger.error(e.getMessage());
                }
            } else {
                LoggerProvider.logger.error("An exception was encountered while closing the plugin.", e);
            }
        } finally {
            proxyServer.shutdown();
        }
    }

    private void setupLogger(Logger logger) {
        LoggerProvider.logger = new moe.caa.multilogin.api.logger.Logger() {
            @Override
            protected void handleLog(Level level, String message, Throwable throwable) {
                switch (level) {
                    case DEBUG -> logger.debug(message, throwable);
                    case INFO -> logger.info(message, throwable);
                    case WARN -> logger.warn(message, throwable);
                    case ERROR -> logger.error(message, throwable);
                    default -> logger.error("[UNKNOWN] {}", message, throwable);
                }
            }
        };
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getTempFolder() {
        return tempFolder;
    }

    @Override
    public IScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getPlatformCoreModuleFileName() {
        return "MultiLogin-Velocity-Core";
    }

    @Override
    public IPlatformCore<?> generatePlatformCore(ClassLoader loader) throws Exception {
        Class<?> loaded = loader.loadClass("fun.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore");
        return (IPlatformCore<?>) loaded.getConstructor(MultiLoginVelocityBootstrap.class).newInstance(this);
    }
}
