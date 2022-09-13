package moe.caa.multilogin.fabric.impl;

import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.fabric.inject.mixin.IServerCommandSource_MLA;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;

import java.util.Objects;

/**
 * Fabric 的命令执行者对象
 */
public class FabricSender implements ISender {
    private final ServerCommandSource sender;

    public FabricSender(ServerCommandSource sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        Entity entity = sender.getEntity();
        if (entity != null) {
            return entity instanceof ServerPlayerEntity;
        }
        return false;
    }

    @Override
    public boolean isConsole() {
        return ((IServerCommandSource_MLA) sender).mlHandler_getCommandOutput() instanceof MinecraftServer;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermissionLevel(4);
    }

    @Override
    public void sendMessagePL(String message) {
        for (String s : message.split("\\r?\\n")) {
            sender.sendFeedback(MutableText.of(new LiteralTextContent(message)), false);
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @SneakyThrows
    @Override
    public IPlayer getAsPlayer() {
        return new FabricPlayer(Objects.requireNonNull(sender.getPlayer()));
    }
}
