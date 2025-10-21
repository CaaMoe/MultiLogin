package moe.caa.multilogin.common.internal.command;

import net.kyori.adventure.text.Component;

public record SenderUnwrapper<SENDER>(
        HasPermission<SENDER> hasPermissionFunction,
        SendMessage<SENDER> sendMessageFunction
) {

    public boolean hasPermission(SENDER sender, String permission) {
        return hasPermissionFunction.hasPermission(sender, permission);
    }

    public void sendMessage(SENDER sender, Component component) {
        sendMessageFunction.sendMessage(sender, component);
    }

    public void sendMessage(SENDER sender, Component... components) {
        for (Component component : components) {
            sendMessage(sender, component);
        }
    }

    @FunctionalInterface
    public interface HasPermission<SENDER> {
        boolean hasPermission(SENDER sender, String permission);
    }

    @FunctionalInterface
    public interface SendMessage<SENDER> {
        void sendMessage(SENDER sender, Component component);
    }
}
