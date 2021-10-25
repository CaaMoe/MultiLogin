package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.core.impl.BaseScheduler;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.IServer;
import net.minecraft.server.MinecraftServer;

public class FabricServer implements IServer {
    private final MinecraftServer server;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public FabricServer(MinecraftServer server) {
        this.server = server;
        scheduler = new FabricScheduler();
        playerManager = new FabricPlayerManager(server.getPlayerManager());
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
        return server.getServerModName();
    }

    @Override
    public String getVersion() {
        return server.getVersion();
    }

    @Override
    public void shutdown() {
        server.stop(false);
    }
}
