package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.BaseScheduler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit 端的线程调度器对象
 */
public class BukkitScheduler extends BaseScheduler {
    private final org.bukkit.scheduler.BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    public BukkitScheduler(org.bukkit.scheduler.BukkitScheduler scheduler, JavaPlugin plugin) {
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @Override
    public void runTask(Runnable run, long delay) {
        scheduler.runTaskLater(plugin, run, delay / 50);
    }

    @Override
    public void runTask(Runnable run) {
        scheduler.runTask(plugin, run);
    }
}
