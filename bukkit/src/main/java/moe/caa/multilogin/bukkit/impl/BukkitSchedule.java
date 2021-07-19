package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.Scheduler;

public class BukkitSchedule extends Scheduler {
    private final MultiLoginBukkit PLUGIN;

    public BukkitSchedule(MultiLoginBukkit plugin) {
        PLUGIN = plugin;
    }

    @Override
    public void runTask(Runnable run) {
        PLUGIN.getServer().getScheduler().runTask(PLUGIN, run);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN, run, delay / 50);
    }
}
