package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.ISender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitSender implements ISender {
    private final CommandSender vanHandle;

    public BukkitSender(CommandSender vanHandle) {
        this.vanHandle = vanHandle;
    }

    @Override
    public String getSenderName() {
        return vanHandle.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return vanHandle.hasPermission(permission);
    }

    @Override
    public void sendMessage(BaseComponent text) {
        vanHandle.sendMessage(text);
    }

    @Override
    public boolean isOp() {
        return vanHandle.isOp();
    }

    @Override
    public boolean isPlayer() {
        return vanHandle instanceof Player;
    }
}
