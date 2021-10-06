package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.core.impl.BaseScheduler;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.IServer;
import net.md_5.bungee.BungeeCord;

public class BungeeServer implements IServer {
    private final BungeeCord cord;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public BungeeServer(BungeeCord cord) {
        this.cord = cord;
        scheduler = new BungeeScheduler();
        playerManager = new BungeePlayerManager(cord);
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
        return cord.getName();
    }

    @Override
    public String getVersion() {
        return cord.getVersion();
    }

    @Override
    public void shutdown() {
        cord.stop();
    }
}
