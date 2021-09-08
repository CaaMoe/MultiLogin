package moe.caa.multilogin.bukkit.impl;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.core.impl.ISender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@AllArgsConstructor
public class BukkitSender implements ISender {
    private final CommandSender sender;

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public void kickPlayer(String message) {
        if (isPlayer()) {
            ((Player) sender).kickPlayer(message);
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public UUID getPlayerUid() {
        if (isPlayer()) {
            return ((Player) sender).getUniqueId();
        }
        return null;
    }
}
