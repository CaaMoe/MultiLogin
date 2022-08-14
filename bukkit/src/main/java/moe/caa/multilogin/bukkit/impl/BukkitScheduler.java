package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit 的调度器对象
 */
public class BukkitScheduler extends BaseScheduler {
    private final org.bukkit.scheduler.BukkitScheduler scheduler;
    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin, org.bukkit.scheduler.BukkitScheduler scheduler) {
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
