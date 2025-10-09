package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.api.internal.auth.AuthAPI;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticationService;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationResult;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationService;
import moe.caa.multilogin.core.handle.PlayerHandler;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 验证核心
 */
@Getter
public class AuthHandler implements AuthAPI {
    private final MultiCore core;
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;
    private final ValidateAuthenticationService validateAuthenticationService;


    public AuthHandler(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
        this.validateAuthenticationService = new ValidateAuthenticationService(core);
    }


    /**
     * 开始验证
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       用户IP
     */
    @Override
    public LoginAuthResult auth(String username, String serverId, String ip) {
        YggdrasilAuthenticationResult yggdrasilAuthenticationResult;
        try {
            yggdrasilAuthenticationResult = yggdrasilAuthenticationService.hasJoined(username, serverId, ip);
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.NO_SERVICE) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_failed_no_yggdrasil_service"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_server_down"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.VALIDATION_FAILED) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_validation_failed"));
            }
            if (yggdrasilAuthenticationResult.getReason() != YggdrasilAuthenticationResult.Reason.ALLOWED ||
                    yggdrasilAuthenticationResult.getResponse() == null ||
                    yggdrasilAuthenticationResult.getServiceConfig().getId() == -1) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_unknown"));
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while processing the hasJoined request.", e);
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(null, core.getLanguageHandler().getMessage("auth_yggdrasil_error"));
        }

        return checkIn(yggdrasilAuthenticationResult);
    }

    public LoginAuthResult checkIn(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
        try {
            ValidateAuthenticationResult validateAuthenticationResult = validateAuthenticationService.checkIn(baseServiceAuthenticationResult);
            if (validateAuthenticationResult.getReason() == ValidateAuthenticationResult.Reason.ALLOWED) {
                LoggerProvider.getLogger().info(
                        String.format("%s(uuid: %s) from authentication service %s(sid: %d) has been authenticated, profile redirected to %s(uuid: %s).",
                                baseServiceAuthenticationResult.getResponse().getName(),
                                baseServiceAuthenticationResult.getResponse().getId().toString(),
                                baseServiceAuthenticationResult.getServiceConfig().getName(),
                                baseServiceAuthenticationResult.getServiceConfig().getId(),
                                validateAuthenticationResult.getInGameProfile().getName(),
                                validateAuthenticationResult.getInGameProfile().getId().toString()
                        )
                );
                GameProfile finalProfile = validateAuthenticationResult.getInGameProfile();
                core.getPlayerHandler().getLoginCache().put(finalProfile.getId(), new PlayerHandler.Entry(
                        baseServiceAuthenticationResult.getResponse(),
                        baseServiceAuthenticationResult.getServiceConfig(),
                        System.currentTimeMillis()
                ));
                return LoginAuthResult.ofAllowed(baseServiceAuthenticationResult, validateAuthenticationResult, finalProfile);
            }
            return LoginAuthResult.ofDisallowedByValidateAuthenticator(baseServiceAuthenticationResult, validateAuthenticationResult, validateAuthenticationResult.getDisallowedMessage());
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while processing the validation request.", e);
            return LoginAuthResult.ofDisallowedByValidateAuthenticator(baseServiceAuthenticationResult, null, core.getLanguageHandler().getMessage("auth_validate_error"));
        }
    }
}
