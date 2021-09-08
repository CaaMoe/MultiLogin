package moe.caa.multilogin.bukkit.impl;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.AbstractScheduler;

@AllArgsConstructor
public class BukkitScheduler extends AbstractScheduler {
    private final MultiLoginBukkit plugin;

    @Override
    public void runTask(Runnable run, long delay) {
        plugin.getServer().getScheduler().runTaskLater(plugin, run, delay / 50);
    }

    @Override
    public void runTask(Runnable run) {
        plugin.getServer().getScheduler().runTask(plugin, run);
    }
}
