package moe.caa.multilogin.api;

import moe.caa.multilogin.api.data.MultiLoginPlayerData;
import moe.caa.multilogin.api.service.IService;

import java.util.Collection;
import java.util.UUID;

public interface MultiLoginAPI {
    Collection<? extends IService> getServices();

    MultiLoginPlayerData getPlayerData(UUID inGameUUID);
}
