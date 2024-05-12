package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.IPlayerManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Velocity 玩家管理器对象
 */
public class VelocityPlayerManager implements IPlayerManager {
    private final ProxyServer server;

    public VelocityPlayerManager(ProxyServer server) {
        this.server = server;
    }

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return server.getAllPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(name)).map(VelocityPlayer::new).collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        Optional<Player> player = server.getPlayer(uuid);
        return player.map(VelocityPlayer::new).orElse(null);
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return server.getAllPlayers().stream().map(VelocityPlayer::new).collect(Collectors.toSet());
    }
}
