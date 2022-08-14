package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bukkit 的玩家管理器对象
 */
public class BukkitPlayerManager implements IPlayerManager {
    private final Server server;

    public BukkitPlayerManager(Server server) {
        this.server = server;
    }

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return server.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(name)).map(BukkitPlayer::new).collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        Player player = server.getPlayer(uuid);
        if (player != null) return new BukkitPlayer(player);
        return null;
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return server.getOnlinePlayers().stream().map(BukkitPlayer::new).collect(Collectors.toSet());
    }
}
