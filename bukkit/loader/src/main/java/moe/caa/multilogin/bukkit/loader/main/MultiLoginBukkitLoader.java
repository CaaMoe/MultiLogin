package moe.caa.multilogin.bukkit.loader.main;

import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Bukkit 插件引导程序
 */
public class MultiLoginBukkitLoader extends JavaPlugin implements IPluginLoader {
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
                    "moe.caa.multilogin.bukkit.main.MultiLoginBukkitPluginBootstrap",
                    new Class[]{MultiLoginBukkitLoader.class, Server.class}, new Object[]{this, getServer()});
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
        getServer().shutdown();
    }

    @Override
    public String getSectionJarFileName() {
        return "MultiLogin-Bukkit.JarFile";
    }

    @Override
    public void shutdown() {
        getServer().shutdown();
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }
}
