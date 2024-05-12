package moe.caa.multilogin.api.service;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface IService {
    int getServiceId();

    @NotNull String getServiceName();

    @NotNull ServiceType getServiceType();
}
