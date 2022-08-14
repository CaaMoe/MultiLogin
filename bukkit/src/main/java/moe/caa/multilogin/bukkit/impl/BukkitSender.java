package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Bukkit 的命令执行者对象
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
    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessagePL(String message) {
        for (String s : message.split("\\r?\\n")) {
            sender.sendMessage(s);
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public IPlayer getAsPlayer() {
        return new BukkitPlayer((Player) sender);
    }
}
