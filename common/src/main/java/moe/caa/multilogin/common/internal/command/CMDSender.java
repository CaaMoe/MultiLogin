package moe.caa.multilogin.common.internal.command;

import net.kyori.adventure.text.Component;

public interface CMDSender {
    boolean hasPermission(String permission);

    void sendMessage(Component component);
}
