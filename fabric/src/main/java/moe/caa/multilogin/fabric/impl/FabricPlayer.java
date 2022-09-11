package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.net.SocketAddress;
import java.util.UUID;

public class FabricPlayer extends FabricSender implements IPlayer {
    private final ServerPlayerEntity player;

    public FabricPlayer(ServerPlayerEntity player) {
        super(player.getCommandSource());
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.networkHandler.disconnect(new LiteralText(message));
    }

    @Override
    public UUID getUniqueId() {
        return player.getGameProfile().getId();
    }

    @Override
    public SocketAddress getAddress() {
        return player.networkHandler.getConnection().getAddress();
    }
}
