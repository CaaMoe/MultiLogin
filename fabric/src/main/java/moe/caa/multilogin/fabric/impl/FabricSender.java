package moe.caa.multilogin.fabric.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.impl.IPlayer;
import moe.caa.multilogin.core.impl.ISender;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

@AllArgsConstructor
public class FabricSender implements ISender {
    private final ServerCommandSource source;

    @Override
    public boolean isPlayer() {
        Entity entity = source.getEntity();
        if (entity != null) {
            return entity instanceof ServerPlayerEntity;
        }
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermissionLevel(4);
    }

    @Override
    public void sendMessage(String message) {
        source.sendFeedback(new LiteralText(message), false);
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @SneakyThrows
    @Override
    public IPlayer getAsPlayer() {
        return new FabricPlayer(source.getPlayer());
    }
}
