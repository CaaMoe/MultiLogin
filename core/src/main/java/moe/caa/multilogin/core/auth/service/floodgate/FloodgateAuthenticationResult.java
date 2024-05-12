package moe.caa.multilogin.core.auth.service.floodgate;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;
import moe.caa.multilogin.core.configuration.service.FloodgateServiceConfig;

public class FloodgateAuthenticationResult extends BaseServiceAuthenticationResult {
    public FloodgateAuthenticationResult(GameProfile response, FloodgateServiceConfig serviceConfig) {
        super(response, serviceConfig);
    }

    @Override
    public boolean isAllowed() {
        return true;
    }
}
