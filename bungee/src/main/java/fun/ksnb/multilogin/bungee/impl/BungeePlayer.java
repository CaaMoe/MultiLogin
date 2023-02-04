package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.util.Pair;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * Bungee 的玩家对象
 */
public class BungeePlayer extends BungeeSender implements IPlayer {
    private final ProxiedPlayer player;

    public BungeePlayer(ProxiedPlayer player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.disconnect(new TextComponent(message));
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public SocketAddress getAddress() {
        return player.getSocketAddress();
    }

    @Override
    public void resetGameProfile(GameProfile infoPair) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reconnect() throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeePlayer that = (BungeePlayer) o;
        return Objects.equals(player.getUniqueId(), that.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getUniqueId());
    }
}
