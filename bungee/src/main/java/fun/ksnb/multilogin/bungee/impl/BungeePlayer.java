package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.core.impl.IPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeePlayer extends BungeeSender implements IPlayer {
    private final ProxiedPlayer player;

    public BungeePlayer(ProxiedPlayer player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.disconnect(message);
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
}
