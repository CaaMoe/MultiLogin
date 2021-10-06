package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.core.impl.IPlayer;
import moe.caa.multilogin.core.impl.IPlayerManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeePlayerManager implements IPlayerManager {
    private final BungeeCord cord;

    public BungeePlayerManager(BungeeCord cord) {
        this.cord = cord;
    }

    @Override
    public Set<IPlayer> getPlayer(String name) {
        Set<IPlayer> ret = new HashSet<>();
        for (ProxiedPlayer player : cord.getPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                ret.add(new BungeePlayer(player));
            }
        }
        return ret;
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        ProxiedPlayer player = cord.getPlayer(uuid);
        if (player == null) return null;
        return new BungeePlayer(player);
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return cord.getPlayers().stream().map(BungeePlayer::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isOnlineMode() {
        return cord.getConfig().isOnlineMode();
    }

    @Override
    public boolean isWhitelist() {
        return false;
    }
}
