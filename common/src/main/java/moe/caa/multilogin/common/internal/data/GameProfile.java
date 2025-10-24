package moe.caa.multilogin.common.internal.data;

import java.util.List;
import java.util.UUID;

public record GameProfile(
        UUID uuid,
        String username,
        List<Property> properties
) {
    public record Property(
            String name,
            String value,
            String signature
    ) {
    }
}