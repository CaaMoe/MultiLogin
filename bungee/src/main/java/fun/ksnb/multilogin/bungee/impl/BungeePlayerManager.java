package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeePlayerManager implements IPlayerManager {

    private final ProxyServer bungeeCord;

    public BungeePlayerManager(ProxyServer bungeeCord) {
        this.bungeeCord = bungeeCord;
    }

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return bungeeCord.getPlayers().stream().filter(p -> p.getName().equalsIgnoreCase(name))
                .map(BungeePlayer::new).collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        ProxiedPlayer player = bungeeCord.getPlayer(uuid);
        return player == null ? null : new BungeePlayer(player);
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return bungeeCord.getPlayers().stream().map(BungeePlayer::new).collect(Collectors.toSet());
    }
}
