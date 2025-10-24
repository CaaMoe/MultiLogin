package moe.caa.multilogin.common.internal.data;

import net.kyori.adventure.text.Component;

public interface Sender {
    boolean hasPermission(String permission);

    void sendMessage(Component component);
}
