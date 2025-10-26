package moe.caa.multilogin.common.internal.handler;

import moe.caa.multilogin.common.internal.config.authentication.LocalAuthenticationConfig;
import moe.caa.multilogin.common.internal.data.LoggingUser;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;

public final class DirectlyLoginHandler extends LoginHandler {

    public DirectlyLoginHandler(MultiCore core) {
        super(core);
    }


    private void handleHasJoinedFailedResult(LoggingUser user, LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult result) {
        switch (result) {
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedInvalidSessionResult ignored -> {
                core.platform.getPlatformLogger().warn("Player " + user.getExpectUsername() + " tried to join with an invalid session.");
                user.closeConnect(core.messageConfig.loginFailedLocalAuthenticationInvalidSession.get().build());
            }
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.hasJoinedFailedServiceUnavailableResult unavailableResult -> {
                core.platform.getPlatformLogger().error("Player " + user.getExpectUsername() + " tried to join but the session server was unavailable.", unavailableResult.throwable);
                user.closeConnect(core.messageConfig.loginFailedLocalAuthenticationUnavailable.get().build());
            }
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to verify " + user.getExpectUsername() + " session.", throwResult.throwable);
                user.closeConnect(core.messageConfig.loginUnknownError.get().build());
            }
        }
    }


    void handleDirectlyLogin(LoggingUser loggingUser) throws Throwable {
        core.platform.getPlatformLogger().debug("Start handle the direct login of " + loggingUser.getExpectUsername());

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to direct login, but the server only allowed transfer login(remote authentication only).");
            loggingUser.closeConnect(core.messageConfig.loginFailedDirectAuthenticationOnlyUseTransferLogin.get().build());
            return;
        }

        // 本地验证
        String serverID;
        switch (loggingUser.switchToEncryptedState(true)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult -> {
                handleSwitchToEncryptedFailedResult(loggingUser, failedResult);
                return;
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult succeedResult ->
                    serverID = succeedResult.serverID;
        }

        core.platform.getPlatformLogger().debug("Start verifying the session for direct login of " + loggingUser.getExpectUsername() + "(serverID: " + serverID + ", playerIP: " + loggingUser.getPlayerIP() + ").");
        switch (core.platform.getLocalYggdrasilSessionService().hasJoined(
                serverID,
                loggingUser.getExpectUsername(),
                loggingUser.getPlayerIP())
        ) {
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult failedResult ->
                    handleHasJoinedFailedResult(loggingUser, failedResult);
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedSucceedResult succeedResult ->
                    autoSelectProfileLogin(
                            loggingUser,
                            localAuthenticationConfig,
                            core.databaseHandler.updateOrCreateUser(localAuthenticationConfig, succeedResult.profile),
                            succeedResult.profile
                    );
        }
    }
}
