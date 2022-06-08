package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import moe.caa.multilogin.api.plugin.IPlayer;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.UUID;

public class VelocityPlayer extends VelocitySender implements IPlayer {
    private final Player player;

    public VelocityPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.disconnect(Component.text(message));
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public InetSocketAddress getAddress() {
        return player.getRemoteAddress();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }
}
