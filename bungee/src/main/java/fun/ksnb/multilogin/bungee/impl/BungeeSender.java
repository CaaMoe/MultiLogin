package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

/**
 * Bungee 的命令执行者对象
 */
public class BungeeSender implements ISender {
    private final CommandSender sender;

    public BungeeSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
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
            sender.sendMessage(new TextComponent(s));
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public IPlayer getAsPlayer() {
        return null;
    }
}
