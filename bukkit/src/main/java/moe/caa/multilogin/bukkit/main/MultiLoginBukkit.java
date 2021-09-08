package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.bukkit.impl.BukkitServer;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {
    private MultiCore core;

    @Override
    public void onEnable() {
        core = new MultiCore(this);
        if (!core.init()) setEnabled(false);

    }

    @Override
    public void onDisable() {
        if(core != null) core.disable();
    }

    @Override
    public void initService() {

    }

    @Override
    public void initOther() {

    }

    @Override
    public IServer getRunServer() {
        return new BukkitServer(this, getServer());
    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        Level logLevel;
        if(level == LoggerLevel.INFO) logLevel = Level.INFO;
        else if(level == LoggerLevel.WARN) logLevel = Level.WARNING;
        else if(level == LoggerLevel.ERROR) logLevel = Level.SEVERE;
        else if(level == LoggerLevel.DEBUG) return;
        else logLevel = Level.INFO;
        getLogger().log(logLevel, message, throwable);
    }
}
