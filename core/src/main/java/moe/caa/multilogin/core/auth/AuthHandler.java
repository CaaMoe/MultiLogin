package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationResult;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationService;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationService;
import moe.caa.multilogin.core.main.MultiCore;

public class AuthHandler implements AuthAPI {
    private final MultiCore core;
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;
    private final ValidateAuthenticationService validateAuthenticationService;


    public AuthHandler(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
        this.validateAuthenticationService = new ValidateAuthenticationService(core);
    }

    @Override
    public AuthResult auth(String username, String serverId, String ip) {
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

        try {
            ValidateAuthenticationResult validateAuthenticationResult = validateAuthenticationService.checkIn(username, serverId, ip, yggdrasilAuthenticationResult);
            if (validateAuthenticationResult.getReason() == ValidateAuthenticationResult.Reason.ALLOWED) {
                LoggerProvider.getLogger().info(
                        String.format("The in game uuid of player %s is %s, and the online uuid is %s, which comes from the authentication result of yggdrasil id %d.",
                                validateAuthenticationResult.getInGameProfile().getName(),
                                validateAuthenticationResult.getInGameProfile().getId().toString(),
                                yggdrasilAuthenticationResult.getResponse().getId(),
                                yggdrasilAuthenticationResult.getYggdrasilId()
                        )
                );
                return AuthResult.ofAllowed(validateAuthenticationResult.getInGameProfile());
            }
            return AuthResult.ofDisallowed(validateAuthenticationResult.getDisallowedMessage());
        } catch (Exception e){
            LoggerProvider.getLogger().error("An exception occurred while processing the validation request.", e);
            return AuthResult.ofDisallowed(core.getLanguageHandler().getMessage("auth_validate_error"));
        }
    }
}
