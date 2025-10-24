package moe.caa.multilogin.common.internal.manager;

import moe.caa.multilogin.common.internal.config.AuthenticationConfig;
import moe.caa.multilogin.common.internal.config.LocalAuthenticationConfig;
import moe.caa.multilogin.common.internal.data.GameProfile;
import moe.caa.multilogin.common.internal.data.LoggingUser;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.cookie.CookieData;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;
import moe.caa.multilogin.common.internal.util.Key;
import moe.caa.multilogin.common.internal.util.StringUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

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
                user.closeConnect(core.messageConfig.loginUnknownError.get());
            }
        }
    }

    private void handleFailedResult(LoggingUser user, HandleLoginResult.HandleLoginFailedResult result) {
        Component disconnectReason = switch (result) {
            case LoginManager.HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseReasonResult reasonResult ->
                    reasonResult.reason;
            case LoginManager.HandleLoginResult.HandleLoginFailedResult.HandleLoginFailedBecauseThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to processed login player: " + user.getExpectUsername(), throwResult.throwable);
                yield core.messageConfig.loginUnknownError.get();
            }
        };

        user.closeConnect(disconnectReason);
    }

    private void handleFailedResult(LoggingUser user, LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult result) {
        switch (result) {
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedInvalidSessionResult ignored -> {
                core.platform.getPlatformLogger().warn("Player " + user.getExpectUsername() + " tried to join with an invalid session.");
                user.closeConnect(core.messageConfig.loginFailedLocalAuthenticationInvalidSession.get());
            }
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.hasJoinedFailedServiceUnavailableResult unavailableResult -> {
                core.platform.getPlatformLogger().error("Player " + user.getExpectUsername() + " tried to join but the session server was unavailable.", unavailableResult.throwable);
                user.closeConnect(core.messageConfig.loginFailedLocalAuthenticationUnavailable.get());
            }
            case LocalYggdrasilSessionService.HasJoinedResult.HasJoinedFailedResult.HasJoinedFailedThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to verify " + user.getExpectUsername() + " session.", throwResult.throwable);
                user.closeConnect(core.messageConfig.loginUnknownError.get());
            }
        }
    }

    public void handleLogging(LoggingUser loggingUser) {
        try {
            // 检查 expect name
            if (!core.mainConfig.disableHelloPacketUsernameValidation.get()) {
                if (!StringUtil.isReasonablePlayerName(loggingUser.getExpectUsername())) {
                    core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to login with invalid characters in name.");
                    loggingUser.closeConnect(core.messageConfig.loginHelloPacketInvalidCharactersInName.get());
                }
            }

            // directly login.
            if (!loggingUser.isTransferred()) {
                handleDirectlyLogin(loggingUser);
                return;
            }

            core.platform.getPlatformLogger().debug("Start processing the login(transfer) request of " + loggingUser.getExpectUsername());

            byte[] cookie = loggingUser.requestCookie(new Key("multilogin", "cookie"));
            if (cookie == null || cookie.length == 0) {
                core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to transfer login, but did not carry a valid cookie.");
                // todo 空 cookie, 阻止登录
                return;
            }

            CookieData cookieData = CookieData.deserialize(cookie);
        } catch (Throwable t) {

        }

    }

    private void handleDirectlyLogin(LoggingUser loggingUser) throws Throwable {
        core.platform.getPlatformLogger().debug("Start processing the login(directly) request of " + loggingUser.getExpectUsername());

        LocalAuthenticationConfig localAuthenticationConfig = core.localAuthenticationConfig;
        // 服务器只允许 remote authentication
        if (localAuthenticationConfig == null) {
            core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to login directly, but the server only allowed transfer login(remote authentication only).");
            loggingUser.closeConnect(core.messageConfig.loginFailedRemoteAuthenticationOnly.get());
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

        switch (handleLogged(localAuthenticationConfig, core.userManager.getOrCreateUser(localAuthenticationConfig.id.get(), gameProfile.uuid(), gameProfile.username()), gameProfile)) {
            case HandleLoginResult.HandleLoginFailedResult failedResult -> handleFailedResult(loggingUser, failedResult);
            case HandleLoginResult.HandleLoginSucceedResult succeedResult -> loggingUser.completeLogin(succeedResult.data);
        }
    }


    private HandleLoginResult handleLogged(AuthenticationConfig authentication, UserManager.User user, GameProfile onlineGameProfile) {
        try {
            ProfileManager.Profile profile = null;

            Optional<Integer> selectedProfile = user.selectProfileID();
            if (selectedProfile.isPresent()) {
                // 使用当前选中档案登录
                profile = core.profileManager.getProfileSnapshotByID(selectedProfile.get());
                if (profile == null) {
                    core.platform.getPlatformLogger().error("User " + user.getDisplayName() + " attempted to use selected profile ID " + selectedProfile.get() + " which does not exist. Will try to choose other available profile.");
                    core.userManager.removeUserSelectedProfileID(user.userID());
                }
            }

            if (profile == null) {
                // 使用其他可登录档案
                List<Integer> avaliableProfileIDList = core.userManager.getAvailableProfileIDListByUserID(user.userID());
                for (Integer profileID : avaliableProfileIDList) {
                    profile = core.profileManager.getProfileSnapshotByID(profileID);
                    if (profile != null) {
                        core.userManager.setUserSelectedProfileID(user.userID(), profileID);
                        break;
                    }
                }
            }

            if (profile == null) {
                core.platform.getPlatformLogger().info("User " + user.getDisplayName() + " has no selected profile and no available profiles, Creating new profile...");

                ProfileManager.CreateProfileResult profileCreateResult = core.profileManager.createProfile(
                        user.userUUID(),
                        user.username(),
                        ProfileManager.UUIDConflictPolicy.RANDOM,
                        ProfileManager.NameConflictPolicy.INCREMENT_RIGHT_TRUNCATE
                );
                switch (profileCreateResult) {
                    case ProfileManager.CreateProfileResult.CreateProfileFailedResult createProfileFailedResult -> {
                        return switch (createProfileFailedResult) {
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult enumResult -> {
                                Component disconnectReason = switch (enumResult.reason) {
                                    case UUID_CONFLICT -> core.messageConfig.loginProfileCreateUuidConflict.get();
                                    case NAME_CONFLICT -> core.messageConfig.loginProfileCreateNameConflict.get();
                                    case NAME_AMEND_RESTRICT ->
                                            core.messageConfig.loginProfileCreateNameAmendRestrict.get();
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

                        core.userManager.addUserHaveProfile(user.userID(), profile.profileID());
                        core.userManager.setUserSelectedProfileID(user.userID(), profile.profileID());
                    }
                }
            }
            assert profile != null;

            OnlineData data = new OnlineData(
                    new OnlineData.OnlineUser(user.userID(), authentication, onlineGameProfile),
                    new OnlineData.OnlineProfile(profile.profileID(), profile.profileUUID(), profile.profileName())
            );

            core.platform.getPlatformLogger().info("User " + user.getDisplayName() + " logged in with profile " + profile.getDisplayName());
            return new HandleLoginResult.HandleLoginSucceedResult(data);
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
