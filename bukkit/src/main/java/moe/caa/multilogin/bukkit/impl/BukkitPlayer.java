package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.IPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Bukkit 端的玩家对象
 */
public class BukkitPlayer extends BukkitSender implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.kickPlayer(message);
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
}
