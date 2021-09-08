package moe.caa.multilogin.bukkit.impl;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.core.impl.IPlayerManager;
import moe.caa.multilogin.core.impl.ISender;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BukkitPlayerManager implements IPlayerManager {
    private final Server server;

    @Override
    public Set<ISender> getPlayer(String name) {
        Set<ISender> ret = new HashSet<>();
        for (Player player : server.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                ret.add(new BukkitSender(player));
            }
        }
        return ret;
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        Player ret = server.getPlayer(uuid);
        if (ret == null) return null;
        return new BukkitSender(ret);
    }

    @Override
    public Set<ISender> getOnlinePlayers() {
        return server.getOnlinePlayers().stream().map(BukkitSender::new).collect(Collectors.toSet());
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
