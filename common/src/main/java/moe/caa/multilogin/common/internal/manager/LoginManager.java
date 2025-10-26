package moe.caa.multilogin.common.internal.manager;

import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig;
import moe.caa.multilogin.common.internal.config.authentication.LocalAuthenticationConfig;
import moe.caa.multilogin.common.internal.data.*;
import moe.caa.multilogin.common.internal.data.cookie.ReconnectCookieData;
import moe.caa.multilogin.common.internal.data.cookie.ReconnectSpecifiedProfileIDCookieData;
import moe.caa.multilogin.common.internal.data.cookie.SignedCookieData;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;
import moe.caa.multilogin.common.internal.util.StringUtil;
import net.kyori.adventure.text.Component;

public class LoginManager {
    private final MultiCore core;

    public LoginManager(MultiCore core) {
        this.core = core;
    }

    private void handleFailedResult(LoggingUser user, LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult result) {
        switch (result) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedReasonResult reasonResult -> {
                switch (reasonResult.cause) {
                    case CLOSED -> {
                        user.closeConnection();
                    }
                }
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to encrypt " + user.getExpectUsername() + " connection.", throwResult.throwable);
                user.closeConnect(core.messageConfig.loginUnknownError.get().build());
            }
        }
    }

    private void handleFailedResult(LoggingUser user, HandleLoginResult.HandleLoginFailedResult result) {
        Component disconnectReason = switch (result) {
            case LoginManager.HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseReasonResult reasonResult ->
                    reasonResult.reason;
            case LoginManager.HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to processed login player: " + user.getExpectUsername(), throwResult.throwable);
                yield core.messageConfig.loginUnknownError.get().build();
            }
        };

        user.closeConnect(disconnectReason);
    }

    private void handleFailedResult(LoggingUser user, LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult result) {
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

    public void handleLogging(LoggingUser loggingUser) {
        try {
            // 检查 expect name
            if (!core.mainConfig.disableHelloPacketUsernameValidation.get()) {
                if (!StringUtil.isReasonablePlayerName(loggingUser.getExpectUsername())) {
                    core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to login with invalid characters in name.");
                    loggingUser.closeConnect(core.messageConfig.loginHelloPacketInvalidCharactersInName.get().build());
                }
            }

            // directly login.
            if (!loggingUser.isTransferred()) {
                handleDirectlyLogin(loggingUser);
                return;
            }

            core.platform.getPlatformLogger().debug("Start processing the login(transfer) request of " + loggingUser.getExpectUsername());

            byte[] cookieDataBytes = loggingUser.requestCookie(MultiCore.COOKIE_KEY);
            if (cookieDataBytes == null || cookieDataBytes.length == 0) {
                core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but did not carry a valid cookie.");
                loggingUser.closeConnect(core.messageConfig.loginFailedRemoteAuthenticationNotCarryCookie.get().build());
                return;
            }

            SignedCookieData<?> signedCookieData = SignedCookieData.readSignedCookieData(cookieDataBytes);
            if (signedCookieData.cookieData().isExpired()) {
                core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the cookie(" + signedCookieData.cookieData().getDescription() + ") carried has expired.");
                loggingUser.closeConnect(core.messageConfig.loginFailedRemoteAuthenticationCarryCookieHasExpired.get().build());
                return;
            }

            switch (signedCookieData.cookieData()) {
                case ReconnectSpecifiedProfileIDCookieData cookieData -> {
                    handleReconnectSpecifiedProfileLogin(loggingUser, signedCookieData, cookieData);
                }
                case ReconnectCookieData cookieData -> {
                    handleReconnectLogin(loggingUser, signedCookieData, cookieData);
                }
            }
        } catch (Throwable t) {
            core.platform.getPlatformLogger().error("Failed to processed login player: " + loggingUser.getExpectUsername(), t);
            loggingUser.closeConnect(core.messageConfig.loginUnknownError.get().build());
        }
    }

    private void handleReconnectSpecifiedProfileLogin(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, ReconnectSpecifiedProfileIDCookieData cookieData) throws Throwable {
        // 本机签名验证
        if (!signedCookieData.validateSignature(
                core.mainConfig.localRsa.get().publicKey.get(),
                core.mainConfig.localRsa.get().verifyDigitalSignatureAlgorithm.get()
        )) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the signature of the cookie(" + signedCookieData.cookieData().getDescription() + ") carried was invalid.");
            loggingUser.closeConnect(core.messageConfig.loginFailedReconnectSpecifiedProfileInvalidSignature.get().build());
            return;
        }

        core.platform.getPlatformLogger().debug("Start processing the login(" + cookieData.getDescription() + ") request of " + loggingUser.getExpectUsername());

        int profileID = cookieData.specifiedProfileID;
        int userID = cookieData.userID;
        GameProfile gameProfile = cookieData.authenticatedGameProfile;

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the server only allowed transfer login(remote authentication only).");
        }

        User user = core.databaseHandler.getUserByUserID(userID);
        Profile profile = core.databaseHandler.getProfileByProfileID(profileID);

        if (profile == null || user == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the specified user or profile did not exist.");
        }

        // 请求加密连接
        switch (loggingUser.switchToEncryptedState(false)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult -> {
                handleFailedResult(loggingUser, failedResult);
                return;
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult ignored -> {
            }
        }

        // 完成登录
        OnlineData onlineData = completedLogin(core.localAuthenticationConfig, user, gameProfile, profile);
        loggingUser.completeLogin(onlineData);
    }

    private void handleReconnectLogin(LoggingUser loggingUser, SignedCookieData<?> signedCookieData, ReconnectCookieData cookieData) throws Throwable {
        // 本机签名验证
        if (!signedCookieData.validateSignature(
                core.mainConfig.localRsa.get().publicKey.get(),
                core.mainConfig.localRsa.get().verifyDigitalSignatureAlgorithm.get()
        )) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but the signature of the cookie(" + signedCookieData.cookieData().getDescription() + ") carried was invalid.");
            loggingUser.closeConnect(core.messageConfig.loginFailedReconnectSpecifiedProfileInvalidSignature.get().build());
            return;
        }


        core.platform.getPlatformLogger().debug("Start processing the login(" + cookieData.getDescription() + ") request of " + loggingUser.getExpectUsername());

        int userID = cookieData.userID;
        GameProfile gameProfile = cookieData.authenticatedGameProfile;

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the server only allowed transfer login(remote authentication only).");
        }

        User user = core.databaseHandler.getUserByUserID(userID);

        if (user == null) {
            throw new IllegalStateException("Player " + loggingUser.getExpectUsername() + " tried to transfer login(" + cookieData.getDescription() + "), but the specified user did not exist.");
        }


        switch (loggingUser.switchToEncryptedState(false)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult -> {
                handleFailedResult(loggingUser, failedResult);
                return;
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult succeedResult -> {
            }
        }


        switch (handleLogged(localAuthenticationConfig, user, gameProfile)) {
            case HandleLoginResult.HandleLoginFailedResult failedResult ->
                    handleFailedResult(loggingUser, failedResult);
            case HandleLoginResult.HandleLoginSucceedResult succeedResult ->
                    loggingUser.completeLogin(succeedResult.data);
        }
    }

    private void handleDirectlyLogin(LoggingUser loggingUser) throws Throwable {
        core.platform.getPlatformLogger().debug("Start processing the login(directly) request of " + loggingUser.getExpectUsername());

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to login directly, but the server only allowed transfer login(remote authentication only).");
            loggingUser.closeConnect(core.messageConfig.loginFailedRemoteAuthenticationOnly.get().build());
            return;
        }

        // 本地验证
        String serverID;
        switch (loggingUser.switchToEncryptedState(true)) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult failedResult -> {
                handleFailedResult(loggingUser, failedResult);
                return;
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedSucceedResult succeedResult ->
                    serverID = succeedResult.serverID;
        }

        core.platform.getPlatformLogger().debug("Start verifying the session login of " + loggingUser.getExpectUsername() + "(serverID: " + serverID + ", playerIP: " + loggingUser.getPlayerIP() + ").");
        GameProfile gameProfile;
        switch (core.platform.getLocalYggdrasilSessionService().hasJoined(
                serverID,
                loggingUser.getExpectUsername(),
                loggingUser.getPlayerIP())
        ) {
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult failedResult -> {
                handleFailedResult(loggingUser, failedResult);
                return;
            }
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedSucceedResult succeedResult ->
                    gameProfile = succeedResult.profile;
        }

        switch (handleLogged(localAuthenticationConfig, core.databaseHandler.updateOrCreateUser(localAuthenticationConfig, gameProfile), gameProfile)) {
            case HandleLoginResult.HandleLoginFailedResult failedResult -> handleFailedResult(loggingUser, failedResult);
            case HandleLoginResult.HandleLoginSucceedResult succeedResult -> loggingUser.completeLogin(succeedResult.data);
        }
    }

    private OnlineData completedLogin(AuthenticationConfig authentication, User user, GameProfile authenticatedGameProfile, Profile profile) {
        OnlineData data = new OnlineData(
                new OnlineData.OnlineUser(user.userID, authentication, authenticatedGameProfile),
                new OnlineData.OnlineProfile(profile.profileID, profile.profileSlot, profile.profileUUID, profile.profileName)
        );

        core.platform.getPlatformLogger().info("User " + user.displayName() + " logged in with profile " + profile.displayName());
        return data;
    }

    private HandleLoginResult handleLogged(AuthenticationConfig authentication, User user, GameProfile authenticatedGameProfile) {
        try {
            Profile profile = null;

            Integer currentSelectProfileSlot = core.databaseHandler.getUserCurrentSelectProfileSlot(user.userID);

            if (currentSelectProfileSlot != null) {
                // 使用当前选中档案登录
                profile = core.databaseHandler.getProfileByOwnerIDAndSlotID(user.userID, currentSelectProfileSlot);
                if (profile == null) {
                    core.platform.getPlatformLogger().error("User " + user.displayName() + " tried to use selected profile ID " + currentSelectProfileSlot + " which does not exist. Will try to choose other slot profile.");
                    core.databaseHandler.removeCurrentSelectProfile(user.userID);
                }
            }

            if (profile == null) {
                // 使用其他槽位
                var profiles = core.databaseHandler.getProfilesByOwnerID(user.userID);
                if (!profiles.isEmpty()) {
                    profile = profiles.values().iterator().next();
                    core.databaseHandler.updateUserCurrentSelectProfileSlot(user.userID, profile.profileSlot);
                }
            }

            if (profile == null) {
                core.platform.getPlatformLogger().info("User " + user.displayName() + " has no selected profile and no available profiles, creating new profile in slot 0...");

                ProfileManager.CreateProfileResult profileCreateResult = core.profileManager.createProfile(
                        authentication,
                        user,
                        0
                );
                switch (profileCreateResult) {
                    case ProfileManager.CreateProfileResult.CreateProfileFailedResult createProfileFailedResult -> {
                        return switch (createProfileFailedResult) {
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult enumResult -> {
                                Component disconnectReason = switch (enumResult.reason) {
                                    case UUID_CONFLICT ->
                                            core.messageConfig.loginProfileCreateUuidConflict.get().build();
                                    case NAME_CONFLICT ->
                                            core.messageConfig.loginProfileCreateNameConflict.get().build();
                                    case NAME_AMEND_RESTRICT ->
                                            core.messageConfig.loginProfileCreateNameAmendRestrict.get().build();
                                };

                                yield new HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseReasonResult(disconnectReason);
                            }
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult ->
                                    new HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseThrowResult(new IllegalStateException(
                                            "Failed to create profile during user login.", throwResult.throwable
                                    ));
                        };
                    }
                    case ProfileManager.CreateProfileResult.CreateProfileSucceedResult createProfileSucceedResult -> {
                        profile = createProfileSucceedResult.profile;
                        core.databaseHandler.updateUserCurrentSelectProfileSlot(user.userID, profile.profileSlot);
                    }
                }
            }
            assert profile != null;

            return new HandleLoginResult.HandleLoginSucceedResult(completedLogin(
                    authentication,
                    user,
                    authenticatedGameProfile,
                    profile
            ));
        } catch (Throwable t) {
            return new HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseThrowResult(t);
        }
    }


    public sealed abstract static class HandleLoginResult {

        public sealed static abstract class HandleLoginFailedResult extends HandleLoginResult {
            public final static class HandleLoginFailedBecauseReasonResult extends HandleLoginFailedResult {
                public final Component reason;

                public HandleLoginFailedBecauseReasonResult(Component reason) {
                    this.reason = reason;
                }
            }

            public final static class HandleLoginFailedBecauseThrowResult extends HandleLoginFailedResult {
                public final Throwable throwable;

                HandleLoginFailedBecauseThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }

        public final static class HandleLoginSucceedResult extends HandleLoginResult {
            public final OnlineData data;

            public HandleLoginSucceedResult(OnlineData data) {
                this.data = data;
            }
        }
    }
}
