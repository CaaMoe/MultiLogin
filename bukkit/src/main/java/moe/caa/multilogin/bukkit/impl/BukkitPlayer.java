package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * Bukkit 的玩家对象
 */
public class BukkitPlayer extends BukkitSender implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getServer().getScheduler().runTask(JavaPlugin.getPlugin(MultiLoginBukkit.class), ()->kickPlayer(message));
            return;
        }
        player.kickPlayer(message);
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
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BukkitPlayer that = (BukkitPlayer) o;
        return Objects.equals(player.getUniqueId(), that.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getUniqueId());
    }
}
