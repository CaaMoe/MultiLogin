package moe.caa.multilogin.common.internal.manager;

import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.online.OnlineData;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoginManager {
    private final MultiCore core;

    public LoginManager(MultiCore core) {
        this.core = core;
    }

    public ProcessLoginResult processLogin(UUID userUUID, String username, String loginMethod) {
        try {
            UserManager.User user = core.userManager.getOrCreateUser(loginMethod, userUUID, username);

            ProfileManager.Profile profile = null;

            Optional<Integer> oneTimeLoginProfileID = core.userManager.getAndRemoveOneTimeLoginProfileIDByUserID(user.userID());
            if (oneTimeLoginProfileID.isPresent()) {
                // 一次性登录
                core.platform.getPlatformLogger().info("User " + user.getDisplayName() + " is using one-time login profile ID " + oneTimeLoginProfileID.get());
                profile = core.profileManager.getProfileSnapshotByID(oneTimeLoginProfileID.get());

                if (profile == null) {
                    core.platform.getPlatformLogger().error("User " + user.getDisplayName() + " attempted to use one-time login profile ID " + oneTimeLoginProfileID.get() + " which does not exist. Cancelled one-time login.");
                }
            }


            if (profile == null) {
                Optional<Integer> selectedProfile = user.selectProfileID();
                if (selectedProfile.isPresent()) {
                    // 使用当前选中档案登录
                    profile = core.profileManager.getProfileSnapshotByID(selectedProfile.get());
                    if (profile == null) {
                        core.platform.getPlatformLogger().error("User " + user.getDisplayName() + " attempted to use selected profile ID " + selectedProfile.get() + " which does not exist. Will try to choose other available profile.");
                        core.userManager.removeUserSelectedProfileID(user.userID());
                    }
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

                                yield new ProcessLoginResult.ProcessLoginFailedResult.ProcessLoginFailedBecauseReasonResult(disconnectReason);
                            }
                            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult ->
                                    new ProcessLoginResult.ProcessLoginFailedResult.ProcessLoginFailedBecauseThrowResult(new IllegalStateException(
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
                    new OnlineData.OnlineUser(user.userID(), "official", Component.text("测试登录"), user.userUUID(), user.username()),
                    new OnlineData.OnlineProfile(profile.profileID(), profile.profileUUID(), profile.profileName())
            );

            core.platform.getPlatformLogger().info("User " + user.getDisplayName() + " logged in with profile " + profile.getDisplayName());
            return new ProcessLoginResult.ProcessLoginSucceedResult(data);
        } catch (Throwable t) {
            return new ProcessLoginResult.ProcessLoginFailedResult.ProcessLoginFailedBecauseThrowResult(t);
        }
    }


    public sealed abstract static class ProcessLoginResult {

        public sealed static abstract class ProcessLoginFailedResult extends ProcessLoginResult {
            public final static class ProcessLoginFailedBecauseReasonResult extends ProcessLoginFailedResult {
                public final Component reason;

                public ProcessLoginFailedBecauseReasonResult(Component reason) {
                    this.reason = reason;
                }
            }

            public final static class ProcessLoginFailedBecauseThrowResult extends ProcessLoginFailedResult {
                public final Throwable throwable;

                ProcessLoginFailedBecauseThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }

        public final static class ProcessLoginSucceedResult extends ProcessLoginResult {
            public final OnlineData data;

            public ProcessLoginSucceedResult(OnlineData data) {
                this.data = data;
            }
        }
    }
}
