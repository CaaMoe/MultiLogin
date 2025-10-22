package moe.caa.multilogin.common.internal.user;

import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.profile.ProfileManager;

import java.util.UUID;

public class UserManager {
    private final MultiCore core;

    public UserManager(MultiCore core) {
        this.core = core;
    }

    public GetUserResult getOrCreateUser(String loginMethod, UUID userUUID, String username) {
        User user = core.databaseHandler.getUserByUUIDAndLoginMethod(userUUID, loginMethod);
        if (user != null) return new GetUserResult.GetUserSucceedResult(user);

        ProfileManager.CreateProfileResult profileResult = core.profileManager.createProfile(userUUID, username, ProfileManager.AmendRuleUUID.RANDOM, ProfileManager.AmendRuleName.INCREMENT_NUMBER_AND_RIGHT_TRUNCATE);
        return null;
        //        return switch (profileResult) {
//            case ProfileManager.CreateProfileResult.CreateProfileSucceedResult result -> {
//                user = core.databaseHandler.createUser(userUUID, loginMethod, username, result.profile.profileID());
//                core.platform.getPlatformLogger().info("Created new user: " + username + "(" + userUUID + "), user id: " + user.userID);
//                yield new GetUserResult.GetUserSucceedResult(user);
//            }
//            case ProfileManager.CreateProfileResult.CreateProfileFailedResult result -> {
//                yield new GetUserResult.GetUserFailedResult.GetUserFailedBecauseCreateProfileFailedResult(result);
//            }
//        };
    }

    public record User(
            int userID,
            String loginMethod,
            UUID userUUID,
            String username,
            int selectProfileID
    ) {

    }

    public sealed static class GetUserResult {
        public sealed static class GetUserFailedResult extends GetUserResult {
            public final static class GetUserFailedBecauseCreateProfileFailedResult extends GetUserFailedResult {
                public final ProfileManager.CreateProfileResult.CreateProfileFailedResult reason;

                public GetUserFailedBecauseCreateProfileFailedResult(ProfileManager.CreateProfileResult.CreateProfileFailedResult reason) {
                    this.reason = reason;
                }
            }

            public final static class GetUserFailedBecauseThrowResult extends GetUserFailedResult {
                public final Throwable throwable;

                public GetUserFailedBecauseThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }

        public final static class GetUserSucceedResult extends GetUserResult {
            public final User user;

            public GetUserSucceedResult(User user) {
                this.user = user;
            }
        }
    }
}
