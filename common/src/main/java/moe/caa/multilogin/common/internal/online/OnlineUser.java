package moe.caa.multilogin.common.internal.online;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public record OnlineUser(
        int userID,
        String loginMethod,
        Component loginMethodDisplayName,
        UUID userUUID,
        String username
) {
}
