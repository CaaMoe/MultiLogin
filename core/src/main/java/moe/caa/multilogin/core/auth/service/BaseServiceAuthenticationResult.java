package moe.caa.multilogin.core.auth.service;

import lombok.Getter;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

@Getter
public abstract class BaseServiceAuthenticationResult {
    private final GameProfile response;
    private final BaseServiceConfig serviceConfig;

    public BaseServiceAuthenticationResult(GameProfile response, BaseServiceConfig serviceConfig) {
        this.response = response;
        this.serviceConfig = serviceConfig;
    }

    public abstract boolean isAllowed();
}
