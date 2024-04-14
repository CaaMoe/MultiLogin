package moe.caa.multilogin.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public record GameProfile(@NotNull UUID uuid, @NotNull String name,
                          @NotNull Map<String, GameProperty> properties) {
    public @Nullable GameProperty getProperty(@NotNull String name) {
        return properties.get(name);
    }
}
