package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.core.impl.IPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class FabricPlayer extends FabricSender implements IPlayer {
    private final ServerPlayerEntity playerEntity;

    public FabricPlayer(ServerPlayerEntity playerEntity) {
        super(playerEntity.getCommandSource());
        this.playerEntity = playerEntity;
    }

    @Override
    public void kickPlayer(String message) {
        playerEntity.networkHandler.disconnect(new LiteralText(message));
    }

    @Override
    public UUID getUniqueId() {
        return playerEntity.getGameProfile().getId();
    }
}
