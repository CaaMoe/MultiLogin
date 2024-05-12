package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.internal.plugin.BaseScheduler;
import moe.caa.multilogin.api.internal.plugin.IPlayerManager;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.plugin.IServer;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;

public class BukkitServer implements IServer {
    private final MultiLoginBukkit multiLoginBukkit;
    private final BaseScheduler bkScheduler;
    private final BukkitPlayerManager playerManager;

    public BukkitServer(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
        bkScheduler = new BaseScheduler() {
            @Override
            public void runTask(Runnable run, long delay) {
                multiLoginBukkit.getServer().getScheduler().runTaskLater(multiLoginBukkit, run, delay);
            }
        };
        playerManager = new BukkitPlayerManager(multiLoginBukkit.getServer());
    }

    @Override
    public BaseScheduler getScheduler() {
        return bkScheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public boolean isOnlineMode() {
        return multiLoginBukkit.getServer().getOnlineMode();
    }

    @Override
    public boolean isForwarded() {
        return true;
    }

    @Override
    public String getName() {
        return multiLoginBukkit.getServer().getName();
    }

    @Override
    public String getVersion() {
        return multiLoginBukkit.getServer().getVersion();
    }

    @Override
    public void shutdown() {
        multiLoginBukkit.getServer().shutdown();
    }

    @Override
    public ISender getConsoleSender() {
        return new BukkitSender(multiLoginBukkit.getServer().getConsoleSender());
    }

    @Override
    public boolean pluginHasEnabled(String id) {
        return multiLoginBukkit.getServer().getPluginManager().getPlugin(id) != null;
    }
}
