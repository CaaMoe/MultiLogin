package fun.ksnb.multilogin.bungee.loader.main;

import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

public class MultiLoginBungeeLoader extends Plugin implements IPluginLoader {
    private MultiLoginCoreLoader coreLoader;
    private BasePluginBootstrap pluginBootstrap;

    @Override
    public void onLoad() {
        try {
            coreLoader = new MultiLoginCoreLoader(this);
            if (!coreLoader.start()) {
                shutdown();
                return;
            }
            pluginBootstrap = coreLoader.loadBootstrap(
                    "fun.ksnb.multilogin.bungee.main.MultiLoginBungeePluginBootstrap",
                    new Class[]{Plugin.class, ProxyServer.class}, new Object[]{this, getProxy()});
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
        shutdown();
    }


    @Override
    public String getSectionJarFileName() {
        return "MultiLogin-Bungee.JarFile";
    }

    @Override
    public void shutdown() {
        getProxy().stop();
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }
}
