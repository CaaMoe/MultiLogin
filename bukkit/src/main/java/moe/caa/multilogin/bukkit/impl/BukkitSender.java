package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.ISender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitSender implements ISender {
    private final CommandSender VAN_SENDER;

    public BukkitSender(CommandSender van_sender) {
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
        VAN_SENDER.sendMessage(message);
    }

    @Override
    public boolean isOp() {
        return VAN_SENDER.isOp();
    }

    @Override
    public boolean isPlayer() {
        return VAN_SENDER instanceof Player;
    }

    @Override
    public UUID getPlayerUniqueIdentifier() {
        return isPlayer() ? ((Player) VAN_SENDER).getUniqueId() : null;
    }

    @Override
    public boolean kickPlayer(String message) {
        if (isPlayer()) {
            ((Player) VAN_SENDER).kickPlayer(message);
            return true;
        }
        return false;
    }
}
