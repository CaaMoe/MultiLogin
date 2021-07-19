package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.ISchedule;

public class BukkitSchedule implements ISchedule {
    private final MultiLoginBukkit PLUGIN;

    public BukkitSchedule(MultiLoginBukkit plugin) {
        PLUGIN = plugin;
    }

    @Override
    public void runTaskAsync(Runnable run) {
        PLUGIN.getServer().getScheduler().runTaskAsynchronously(PLUGIN, run);
    }

    @Override
    public void runTaskAsync(Runnable run, long delay) {
        PLUGIN.getServer().getScheduler().runTaskLaterAsynchronously(PLUGIN, run, delay / 50);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long per) {
        PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(PLUGIN, run, delay / 50, per / 50);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN, run, delay / 50);
    }

    @Override
    public void runTask(Runnable run) {
        PLUGIN.getServer().getScheduler().runTask(PLUGIN, run);
    }
}
