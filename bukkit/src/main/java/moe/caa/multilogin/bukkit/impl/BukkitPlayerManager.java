package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.IPlayerManager;
import org.bukkit.Server;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitPlayerManager implements IPlayerManager {
    private final Server server;

    public BukkitPlayerManager(Server server) {
        this.server = server;
    }

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return getOnlinePlayers().stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        return Optional.ofNullable(server.getPlayer(uuid)).map(BukkitPlayer::new).orElse(null);
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return server.getOnlinePlayers().stream().map(BukkitPlayer::new).collect(Collectors.toSet());
    }
}
