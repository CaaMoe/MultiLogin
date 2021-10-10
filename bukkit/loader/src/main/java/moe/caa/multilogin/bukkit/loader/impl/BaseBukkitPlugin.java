package moe.caa.multilogin.bukkit.loader.impl;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.bukkit.loader.main.MultiLoginBukkitLoader;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

@AllArgsConstructor
public abstract class BaseBukkitPlugin {
    private final MultiLoginBukkitLoader loader;
    private final Server server;

    public Server getServer(){
        return server;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public abstract boolean onCommand(CommandSender sender,  Command command,  String label,  String[] args);

    public Logger getLogger(){
        return loader.getLogger();
    }

    public PluginDescriptionFile getDescription(){
        return loader.getDescription();
    }

    public JavaPlugin getThis(){
        return loader;
    }

    public void setEnabled(boolean enabled){
        loader.setEnable(enabled);
    }

    public File getDataFolder(){
        return loader.getDataFolder();
    }
}
