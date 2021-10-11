package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.BaseScheduler;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.IServer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit 端的服务器对象
 */
public class BukkitServer implements IServer {
    private final Server server;
    private final IPlayerManager playerManager;
    private final BaseScheduler scheduler;

    public BukkitServer(Server server, JavaPlugin plugin) {
        this.server = server;
        this.playerManager = new BukkitPlayerManager(server);
        this.scheduler = new BukkitScheduler(server.getScheduler(), plugin);
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
}
