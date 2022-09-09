package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fabric 的玩家管理器对象
 */
public class FabricPlayerManager implements IPlayerManager {
    private final PlayerManager manager;

    public FabricPlayerManager(PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return manager.getPlayerList().stream().filter(p -> p.getGameProfile().getName().equalsIgnoreCase(name)).map(FabricPlayer::new).collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        ServerPlayerEntity player = manager.getPlayer(uuid);
        if (player != null) return new FabricPlayer(player);
        return null;
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return manager.getPlayerList().stream().map(FabricPlayer::new).collect(Collectors.toSet());
    }
}
