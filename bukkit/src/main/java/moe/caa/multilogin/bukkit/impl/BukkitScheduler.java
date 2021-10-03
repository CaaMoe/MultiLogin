package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.BaseScheduler;

/**
 * Bukkit 端的线程调度器对象
 */
public class BukkitScheduler extends BaseScheduler {
    private final org.bukkit.scheduler.BukkitScheduler scheduler;
    private final MultiLoginBukkit plugin;

    public BukkitScheduler(org.bukkit.scheduler.BukkitScheduler scheduler, MultiLoginBukkit plugin) {
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
