package moe.caa.multilogin.common.internal.online;

import java.util.UUID;

public record OnlineUser(
        int userID,
        String loginMethod,
        UUID userUUID,
        String username
) {
}
