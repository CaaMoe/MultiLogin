package moe.caa.multilogin.api;

import moe.caa.multilogin.api.data.MultiLoginPlayerData;
import moe.caa.multilogin.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 *
 */
@ApiStatus.NonExtendable
public interface MultiLoginAPI {
    @NotNull Collection<? extends IService> getServices();

    @NotNull MultiLoginPlayerData getPlayerData(@NotNull UUID inGameUUID);
}
