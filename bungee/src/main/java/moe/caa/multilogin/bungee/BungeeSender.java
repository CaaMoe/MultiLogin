package moe.caa.multilogin.bungee;

import moe.caa.multilogin.core.impl.ISender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeSender implements ISender {
    private final CommandSender VAN_SENDER;

    public BungeeSender(CommandSender van_sender) {
        VAN_SENDER = van_sender;
    }

    @Override
    public String getName() {
        return VAN_SENDER.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return VAN_SENDER.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        VAN_SENDER.sendMessage(new TextComponent(message));
    }

    @Override
    public boolean isOp() {
        // TODO: 2021/7/17 ???
        return false;
    }

    @Override
    public boolean isPlayer() {
        return VAN_SENDER instanceof ProxiedPlayer;
    }

    @Override
    public UUID getPlayerUniqueIdentifier() {
        return isPlayer() ? ((ProxiedPlayer) VAN_SENDER).getUniqueId() : null;
    }

    @Override
    public boolean kickPlayer(String message) {
        if (isPlayer()) {
            ((ProxiedPlayer) VAN_SENDER).disconnect(new TextComponent(message));
            return true;
        }
        return false;
    }
}
