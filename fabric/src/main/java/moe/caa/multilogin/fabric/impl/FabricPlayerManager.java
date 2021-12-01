package moe.caa.multilogin.fabric.impl;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.core.impl.IPlayer;
import moe.caa.multilogin.core.impl.IPlayerManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FabricPlayerManager implements IPlayerManager {
    private final PlayerManager playerManager;

    @Override
    public Set<IPlayer> getPlayer(String name) {
        Set<IPlayer> ret = new HashSet<>();
        for (ServerPlayerEntity player : playerManager.getPlayerList()) {
            if (!player.getGameProfile().getName().equalsIgnoreCase(name)) continue;
            ret.add(new FabricPlayer(player));
        }
        return ret;
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        ServerPlayerEntity playerEntity = playerManager.getPlayer(uuid);
        if (playerEntity != null) return new FabricPlayer(playerEntity);
        return null;
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return playerManager.getPlayerList().stream().map(FabricPlayer::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isOnlineMode() {
        return playerManager.getServer().isOnlineMode();
    }

    @Override
    public boolean isWhitelist() {
        return playerManager.isWhitelistEnabled();
    }
}
