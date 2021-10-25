package moe.caa.multilogin.fabric.main;

import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.logging.Level;

public class MultiLoginFabricModBootstrap extends BasePluginBootstrap implements DedicatedServerModInitializer, IPluginLoader {
    private final Logger logger = LogManager.getLogger("MultiLogin");
    private MultiLoginCoreLoader coreLoader;
    private BasePluginBootstrap pluginBootstrap;

    @Override
    public void onInitializeServer() {

    }

    @Override
    public void onLoad() {
        try {
            coreLoader = new MultiLoginCoreLoader(this);
            if (!coreLoader.start()) {
                shutdown();
                return;
            }


            pluginBootstrap.onLoad();
        } catch (Throwable throwable) {
            loggerLog(Level.SEVERE, "A FATAL ERROR OCCURRED DURING INITIALIZATION.", throwable);
            onDisable();
        }
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getSectionJarFileName() {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void loggerLog(java.util.logging.Level level, String message, Throwable throwable) {

    }

    @Override
    public File getDataFolder() {
        return null;
    }
}
