package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.IPlayer;
import moe.caa.multilogin.core.impl.IPlayerManager;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bukkit 端的玩家处理器对象
 */
public class BukkitPlayerManager implements IPlayerManager {
    private final Server server;

    public BukkitPlayerManager(Server server) {
        this.server = server;
    }

    @Override
    public Set<IPlayer> getPlayer(String name) {
        Set<IPlayer> ret = new HashSet<>();
        for (Player player : server.getOnlinePlayers()) {
            if (!player.getName().equalsIgnoreCase(name)) continue;
            ret.add(new BukkitPlayer(player));
        }
        return ret;
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        Player player = server.getPlayer(uuid);
        if (player == null) return null;
        return new BukkitPlayer(player);
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return server.getOnlinePlayers().stream().map(BukkitPlayer::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isOnlineMode() {
        return server.getOnlineMode();
    }

    @Override
    public boolean isWhitelist() {
        return server.hasWhitelist();
    }
}
