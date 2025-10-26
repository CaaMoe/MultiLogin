package moe.caa.multilogin.common.internal.handler;

import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig;
import moe.caa.multilogin.common.internal.data.*;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.util.StringUtil;
import net.kyori.adventure.text.Component;

public sealed class LoginHandler permits DirectlyLoginHandler, TransferLoginHandler {
    protected final MultiCore core;

    public LoginHandler(MultiCore core) {
        this.core = core;
    }

    public static void handleLogging(LoggingUser loggingUser) {
        MultiCore core = MultiCore.instance;
        try {
            if (!core.mainConfig.disableHelloPacketUsernameValidation.get()) {
                if (!StringUtil.isReasonablePlayerName(loggingUser.getExpectUsername())) {
                    core.platform.getPlatformLogger().warn("Player " + loggingUser.getExpectUsername() + " tried to login with invalid characters in name.");
                    loggingUser.closeConnect(core.messageConfig.loginHelloPacketInvalidCharactersInName.get().build());
                }
            }

            if (loggingUser.isTransferred()) {
                core.transferLoginManager.handleTransferLogin(loggingUser);
            } else {
                core.directlyLoginManager.handleDirectlyLogin(loggingUser);
            }
        } catch (Throwable t) {
            core.platform.getPlatformLogger().error("Failed to handle login player: " + loggingUser.getExpectUsername(), t);
            loggingUser.closeConnect(core.messageConfig.loginUnknownError.get().build());
        }
    }

    private void completedLogin(
            LoggingUser loggingUser,
            AuthenticationConfig authentication,
            User user,
            GameProfile authenticatedGameProfile,
            Profile profile
    ) throws Throwable {
        OnlineData data = new OnlineData(
                new OnlineData.OnlineUser(user.userID, authentication, authenticatedGameProfile),
                new OnlineData.OnlineProfile(profile.profileID, profile.profileSlot, profile.profileUUID, profile.profileName)
        );

        core.platform.getPlatformLogger().info("User " + user.displayName() + " logged in with profile " + profile.displayName());
        loggingUser.completeLogin(data);
    }

    protected void handleSwitchToEncryptedFailedResult(LoggingUser user, LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult result) {
        switch (result) {
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedReasonResult reasonResult -> {
                switch (reasonResult.cause) {
                    case CLOSED -> user.closeConnection();
                }
            }
            case LoggingUser.SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedThrowResult throwResult -> {
                core.platform.getPlatformLogger().error("Failed to encrypt " + user.getExpectUsername() + " connection.", throwResult.throwable);
                user.closeConnect(core.messageConfig.loginUnknownError.get().build());
            }
        }
    }

    protected void specifiedProfileLogin(LoggingUser loggingUser, AuthenticationConfig authentication, User user, GameProfile authenticatedGameProfile, Profile profile) throws Throwable {
        core.platform.getPlatformLogger().debug("User " + user.displayName() + " has logged in and specified profile " + profile.displayName() + ".");

        completedLogin(loggingUser, authentication, user, authenticatedGameProfile, profile);
    }

    protected void autoSelectProfileLogin(LoggingUser loggingUser, AuthenticationConfig authentication, User user, GameProfile authenticatedGameProfile) throws Throwable {
        core.platform.getPlatformLogger().debug("User " + user.displayName() + " has logged in and is now selecting a profile.");

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
            core.platform.getPlatformLogger().info("User " + user.displayName() + " did not select any profile and has no available profiles. Creating an initial profile in their slot 0...");

            ProfileManager.CreateProfileResult profileCreateResult = core.profileManager.createProfile(
                    authentication,
                    user,
                    0
            );
            switch (profileCreateResult) {
                case ProfileManager.CreateProfileResult.CreateProfileFailedResult createProfileFailedResult -> {
                    switch (createProfileFailedResult) {
                        case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult enumResult -> {
                            Component disconnectReason = switch (enumResult.reason) {
                                case UUID_CONFLICT -> core.messageConfig.loginProfileCreateUuidConflict.get().build();
                                case NAME_CONFLICT -> core.messageConfig.loginProfileCreateNameConflict.get().build();
                                case NAME_AMEND_RESTRICT ->
                                        core.messageConfig.loginProfileCreateNameAmendRestrict.get().build();
                            };
                            core.platform.getPlatformLogger().warn("Restricted while creating an initial profile for user " + user.displayName() + ".(reason: " + enumResult.reason + ")");
                            loggingUser.closeConnect(disconnectReason);
                        }
                        case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult ->
                                throw new IllegalStateException("Failed to create profile during user login.", throwResult.throwable);
                    }
                }
                case ProfileManager.CreateProfileResult.CreateProfileSucceedResult createProfileSucceedResult -> {
                    profile = createProfileSucceedResult.profile;
                    core.databaseHandler.updateUserCurrentSelectProfileSlot(user.userID, profile.profileSlot);
                }
            }
        }
        assert profile != null;

        completedLogin(loggingUser, authentication, user, authenticatedGameProfile, profile);
    }
}
