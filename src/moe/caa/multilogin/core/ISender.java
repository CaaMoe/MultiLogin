package moe.caa.multilogin.core;

import net.md_5.bungee.api.chat.BaseComponent;

public interface ISender {
    String getSenderName();

    boolean hasPermission(String permission);

    void sendMessage(BaseComponent text);

    boolean isOp();
}
