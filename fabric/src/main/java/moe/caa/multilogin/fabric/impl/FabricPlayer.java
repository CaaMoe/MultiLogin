package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * Fabric 的玩家对象
 */
public class FabricPlayer extends FabricSender implements IPlayer {
    private final ServerPlayerEntity player;

    public FabricPlayer(ServerPlayerEntity player) {
        super(player.getCommandSource());
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        player.networkHandler.disconnect(MutableText.of(new LiteralTextContent(message)));
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
