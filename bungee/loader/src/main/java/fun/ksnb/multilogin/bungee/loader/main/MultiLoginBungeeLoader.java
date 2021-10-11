package fun.ksnb.multilogin.bungee.loader.main;

import fun.ksnb.multilogin.bungee.loader.impl.BaseBungeePlugin;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.loader.impl.IPluginLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

public class MultiLoginBungeeLoader extends Plugin implements IPluginLoader {
    private MultiLoginCoreLoader coreLoader;
    private BaseBungeePlugin baseBungeePlugin;

    @SneakyThrows
    @Override
    public void onLoad() {
        coreLoader = new MultiLoginCoreLoader(this);
        boolean b = coreLoader.start("MultiLogin-Bungee.JarFile");
        if (!b) {
            BungeeCord.getInstance().stop();
            return;
        }

        Class<?> baseBungeePluginClass = Class.forName("fun.ksnb.multilogin.bungee.main.MultiLoginBungee", true, coreLoader.getCurrentUrlClassLoader());
        Constructor<?> constructor = baseBungeePluginClass.getConstructor(MultiLoginBungeeLoader.class);
        this.baseBungeePlugin = (BaseBungeePlugin) constructor.newInstance(this);

        baseBungeePlugin.onLoad();
    }

    @Override
    public void onEnable() {
        if (baseBungeePlugin != null) baseBungeePlugin.onEnable();
    }

    @Override
    public void onDisable() {
        if (baseBungeePlugin != null) baseBungeePlugin.onDisable();
        baseBungeePlugin = null;
        coreLoader.close();
    }


    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }
}
