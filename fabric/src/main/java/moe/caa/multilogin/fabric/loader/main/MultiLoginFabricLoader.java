package moe.caa.multilogin.fabric.loader.main;

import lombok.Getter;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.LoaderType;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import moe.caa.multilogin.fabric.main.MultiLoginFabricPluginBootstrap;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.logging.Level;

public class MultiLoginFabricLoader extends BasePluginBootstrap implements IPluginLoader {

    @Getter
    private final Logger logger = LogManager.getLogger("MultiLogin");
    private final MinecraftServer server;
    private final File dataFolder = new File("config/multilogin");
    private MultiLoginCoreLoader coreLoader;
    private BasePluginBootstrap pluginBootstrap;

    public MultiLoginFabricLoader(MinecraftServer server) {
        this.server = server;
    }


    @Override
    public void onLoad() {
        try {
            coreLoader = new MultiLoginCoreLoader(this, LoaderType.SUPER_REFLECT);
            if (!coreLoader.startLoading()) {
                shutdown();
                return;
            }

            pluginBootstrap = new MultiLoginFabricPluginBootstrap(this, server);
            pluginBootstrap.onLoad();
        } catch (Throwable throwable) {
            loggerLog(Level.SEVERE, "A FATAL ERROR OCCURRED DURING INITIALIZATION.", throwable);
            onDisable();
        }
    }

    @Override
    public void onEnable() {
        if (pluginBootstrap != null) pluginBootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        if (pluginBootstrap != null) pluginBootstrap.onDisable();
        pluginBootstrap = null;
        coreLoader.close();
        server.close();
    }

    @Override
    public String getSectionJarFileName() {
        return null;
    }

    @Override
    public void shutdown() {
        server.close();
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        if (level == Level.SEVERE) logger.error(message, throwable);
        else if (level == Level.WARNING) logger.warn(message, throwable);
        else if (level == Level.INFO) logger.info(message, throwable);
        else logger.info(message, throwable);
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}
