package moe.caa.multilogin.bukkit;

import moe.caa.multilogin.core.ISender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

public class BukkitSender implements ISender {
    private final CommandSender vanSender;

    public BukkitSender(CommandSender vanSender) {
        this.vanSender = vanSender;
    }

    @Override
    public String getSenderName() {
        return vanSender.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return vanSender.hasPermission(permission);
    }

    @Override
    public void sendMessage(BaseComponent text) {
        vanSender.sendMessage(text);
    }

    @Override
    public boolean isOp() {
        return vanSender.isOp();
    }
}
