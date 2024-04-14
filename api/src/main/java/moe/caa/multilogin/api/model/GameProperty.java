package moe.caa.multilogin.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GameProperty(@NotNull String name, @NotNull String value, @Nullable String signature) {
}
