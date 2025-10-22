package moe.caa.multilogin.common.internal.online;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public record OnlineData(OnlineUser onlineUser, OnlineProfile onlineProfile) {
    public record OnlineProfile(
            int profileID,
            UUID profileUUID,
            String profileName
    ) {
    }

    public record OnlineUser(
            int userID,
            String loginMethod,
            Component loginMethodDisplayName,
            UUID userUUID,
            String username
    ) {
    }
}
