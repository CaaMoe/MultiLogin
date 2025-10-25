package moe.caa.multilogin.common.internal.data;

import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig;

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
            AuthenticationConfig service,
            GameProfile profile
    ) {
    }
}
