package moe.caa.multilogin.api.profile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record GameProfile(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull List<Property> properties
) {
    public record Property(
            @NotNull String name,
            @NotNull String value,
            @Nullable String signature
    ) {
    }
}