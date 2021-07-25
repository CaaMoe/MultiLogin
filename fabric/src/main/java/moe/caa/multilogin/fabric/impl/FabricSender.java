package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.core.impl.ISender;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class FabricSender implements ISender {
    private final ServerCommandSource source;

    public FabricSender(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public void sendMessage(String message) {
        source.sendFeedback(new LiteralText(message), false);
    }

    @Override
    public boolean isOp() {
        return source.hasPermissionLevel(1);
    }

    @Override
    public boolean isPlayer() {
        Entity entity = source.getEntity();
        if(entity != null){
            return entity instanceof ServerPlayerEntity;
        }
        return false;
    }

    @Override
    public UUID getPlayerUniqueIdentifier() {
        return isPlayer() ? source.getEntity().getUuid() : null;
    }

    @Override
    public boolean kickPlayer(String message) {
        if (isPlayer()) {
            ((ServerPlayerEntity) source.getEntity()).networkHandler.disconnect(new LiteralText(message));
            return true;
        }
        return false;
    }
}
