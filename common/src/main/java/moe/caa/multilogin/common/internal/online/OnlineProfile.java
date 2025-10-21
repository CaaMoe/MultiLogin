package moe.caa.multilogin.common.internal.online;

import java.util.UUID;

public record OnlineProfile(
        int profileID,
        UUID profileUUID,
        String profileName
) {
}
