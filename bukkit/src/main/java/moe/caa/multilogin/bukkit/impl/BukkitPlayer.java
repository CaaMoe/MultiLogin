package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.entity.Player;

import java.net.SocketAddress;
import java.util.UUID;

public class BukkitPlayer extends BukkitSender implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        MultiLoginBukkit.getInstance().getServer().getScheduler().runTask(MultiLoginBukkit.getInstance(), () -> {
            player.kickPlayer(message);
        });
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public SocketAddress getAddress() {
        return player.getAddress();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
}
