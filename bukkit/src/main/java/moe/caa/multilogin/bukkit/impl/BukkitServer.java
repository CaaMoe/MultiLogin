package moe.caa.multilogin.bukkit.impl;

import lombok.Getter;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.IServer;
import org.bukkit.Server;

public class BukkitServer implements IServer {
    private final Server server;

    @Getter
    private final AbstractScheduler scheduler;

    @Getter
    private final IPlayerManager playerManager;

    public BukkitServer(MultiLoginBukkit plugin, Server server) {
        this.server = server;
        this.scheduler = new BukkitScheduler(plugin);
        this.playerManager = new BukkitPlayerManager(server);
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
