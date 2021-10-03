package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.ISender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Bukkit 端的命令执行者对象
 */
public class BukkitSender implements ISender {
    private final CommandSender sender;

    public BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

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
    public String getName() {
        return sender.getName();
    }
}
