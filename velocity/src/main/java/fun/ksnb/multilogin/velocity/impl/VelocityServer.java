package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.core.impl.BaseScheduler;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.IServer;

public class VelocityServer implements IServer {
    private final ProxyServer server;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public VelocityServer(ProxyServer server) {
        this.server = server;
        this.scheduler = new VelocityScheduler();
        this.playerManager = new VelocityPlayerManager(server);
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
        return  server.getVersion().getName();
    }

    @Override
    public String getVersion() {
        return server.getVersion().getVersion();
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }
}
