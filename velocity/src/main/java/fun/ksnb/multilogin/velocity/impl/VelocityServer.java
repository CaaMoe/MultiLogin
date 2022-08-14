package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.config.PlayerInfoForwarding;
import com.velocitypowered.proxy.config.VelocityConfiguration;
import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.plugin.IServer;

/**
 * Velocity 服务器对象
 */
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
    public boolean isOnlineMode() {
        return server.getConfiguration().isOnlineMode();
    }

    @Override
    public boolean isForwarded() {
        return ((VelocityConfiguration) server.getConfiguration()).getPlayerInfoForwardingMode() != PlayerInfoForwarding.NONE;
    }

    @Override
    public String getName() {
        return server.getVersion().getName();
    }

    @Override
    public String getVersion() {
        return server.getVersion().getVersion();
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public ISender getConsoleSender() {
        return new VelocitySender(server.getConsoleCommandSource());
    }
}
