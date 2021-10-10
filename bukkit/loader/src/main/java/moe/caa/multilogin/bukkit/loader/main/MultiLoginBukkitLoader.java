package moe.caa.multilogin.bukkit.loader.main;

import lombok.SneakyThrows;
import moe.caa.multilogin.core.loader.impl.ISectionLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MultiLoginBukkitLoader extends JavaPlugin implements ISectionLoader {

    @SneakyThrows
    @Override
    public void onEnable() {
        MultiLoginCoreLoader coreLoader = new MultiLoginCoreLoader(this);
        coreLoader.start("");
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }
}
