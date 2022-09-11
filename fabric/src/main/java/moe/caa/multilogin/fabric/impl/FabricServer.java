package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.plugin.IServer;
import net.minecraft.server.MinecraftServer;

public class FabricServer implements IServer {
    private final MinecraftServer server;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public FabricServer(MinecraftServer server) {
        this.server = server;
        this.scheduler = new FabricScheduler();
        this.playerManager = new FabricPlayerManager(server.getPlayerManager());
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
        return server.isOnlineMode();
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
        server.stop(false);
    }

    @Override
    public ISender getConsoleSender() {
        return new FabricSender(server.getCommandSource());
    }
}
