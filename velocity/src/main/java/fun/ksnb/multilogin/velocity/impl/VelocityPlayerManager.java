package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.IPlayerManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VelocityPlayerManager implements IPlayerManager {
    private final ProxyServer server;

    public VelocityPlayerManager(ProxyServer server) {
        this.server = server;
    }

    @Override
    public Set<IPlayer> getPlayer(String name) {
        Set<IPlayer> ret = new HashSet<>();
        for (Player player : server.getAllPlayers()) {
            if (player.getUsername().equalsIgnoreCase(name)) {
                ret.add(new VelocityPlayer(player));
            }
        }
        return ret;
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
