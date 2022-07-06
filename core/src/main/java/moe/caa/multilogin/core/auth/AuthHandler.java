package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationService;
import moe.caa.multilogin.core.main.MultiCore;

public class AuthHandler implements AuthAPI {
    private final MultiCore core;
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;


    public AuthHandler(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
    }

    @Override
    public AuthResult auth(String username, String serverId, String ip) {

        // HasJoined
        YggdrasilAuthenticationResult yggdrasilAuthenticationResult;
        try {
            yggdrasilAuthenticationResult = yggdrasilAuthenticationService.hasJoined(username, serverId, ip);
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.NO_SERVICE) {
                return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_no_server"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
                return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_server_down"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.VALIDATION_FAILED) {
                return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_validation_failed"));
            }
            if (yggdrasilAuthenticationResult.getReason() != YggdrasilAuthenticationResult.Reason.ALLOWED ||
                    yggdrasilAuthenticationResult.getResponse() == null ||
                    yggdrasilAuthenticationResult.getYggdrasilId() == -1) {
                return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_unknown"));
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while processing the hasJoined request.", e);
            return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_yggdrasil_error"));
        }

        return AuthResult.ofAllowed(yggdrasilAuthenticationResult.getResponse());
    }
}
