package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.plugin.IServer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit 的服务端对象
 */
public class BukkitServer implements IServer {
    private final Server server;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;


    public BukkitServer(Plugin plugin, Server server) {
        this.server = server;
        this.scheduler = new BukkitScheduler(plugin, server.getScheduler());
        this.playerManager = new BukkitPlayerManager(server);
    }

    @Override
    public BaseScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public boolean isOnlineMode() {
        return server.getOnlineMode();
    }

    @Override
    public boolean isForwarded() {
        return true;
    }

    @Override
    public String getName() {
        return server.getName();
    }

    @Override
    public String getVersion() {
        return server.getVersion();
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public ISender getConsoleSender() {
        return new BukkitSender(server.getConsoleSender());
    }
}
