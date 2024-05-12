package moe.caa.multilogin.core.configuration.service;

import moe.caa.multilogin.api.service.ServiceType;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import org.jetbrains.annotations.NotNull;

public class FloodgateServiceConfig extends BaseServiceConfig {
    public FloodgateServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat,
                                  boolean whitelist, SkinRestorerConfig skinRestorer) throws ConfException {
        super(id, name, initUUID, initNameFormat, whitelist, skinRestorer);
    }

    @NotNull
    @Override
    public ServiceType getServiceType() {
        return ServiceType.FLOODGATE;
    }
}
