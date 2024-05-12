package moe.caa.multilogin.core.configuration.service;

import moe.caa.multilogin.api.service.ServiceType;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;

public class FloodgateServiceConfig extends BaseServiceConfig {
    public FloodgateServiceConfig(int id, String name, InitUUID initUUID,
                                  boolean whitelist, SkinRestorerConfig skinRestorer) throws ConfException {
        super(id, name, initUUID, whitelist, skinRestorer);
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.FLOODGATE;
    }
}
