package moe.caa.multilogin.bukkit.loader.main;

import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.loader.impl.BaseBukkitPlugin;
import moe.caa.multilogin.core.loader.impl.ISectionLoader;
import moe.caa.multilogin.core.loader.main.MultiLoginCoreLoader;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

public class MultiLoginBukkitLoader extends JavaPlugin implements ISectionLoader {
    private MultiLoginCoreLoader coreLoader;
    private JavaPlugin plugin;
    private BaseBukkitPlugin baseBukkitPlugin;

    @SneakyThrows
    @Override
    public void onLoad() {
        plugin = this;
        coreLoader = new MultiLoginCoreLoader(this);
        boolean b = coreLoader.start("MultiLogin-Bukkit.JarFile");
        if(!b) return;

        Class<?> baseBukkitPluginClass = Class.forName("moe.caa.multilogin.bukkit.main.MultiLoginBukkit", true, coreLoader.getCurrentUrlClassLoader());
        Constructor<?> constructor = baseBukkitPluginClass.getConstructor(MultiLoginBukkitLoader.class, Server.class);
        this.baseBukkitPlugin = (BaseBukkitPlugin) constructor.newInstance(this, getServer());
    }

    @Override
    public void onEnable() {
        if(baseBukkitPlugin != null) baseBukkitPlugin.onEnable();
    }

    @Override
    public void onDisable() {
        if(baseBukkitPlugin != null) baseBukkitPlugin.onDisable();

        baseBukkitPlugin = null;
        coreLoader.close();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(baseBukkitPlugin != null)
            return baseBukkitPlugin.onCommand(sender, command, label, args);
        return false;
    }

    @Override
    public void loggerLog(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }

    public void setEnable(boolean enable) {
        super.setEnabled(enable);
    }
}
