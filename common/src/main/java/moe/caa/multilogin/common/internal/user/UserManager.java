package moe.caa.multilogin.common.internal.user;

import moe.caa.multilogin.common.internal.main.MultiCore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserManager {
    private final MultiCore core;

    public UserManager(MultiCore core) {
        this.core = core;
    }

    public Optional<Integer> getOneTimeLoginProfileIDByUserID(int userID) {
        Optional<Integer> result = Optional.ofNullable(core.databaseHandler.getAndRemoveOneTimeLoginDataByUserID(userID))
                .filter(oneTimeLogin -> oneTimeLogin.expirationTime.isAfter(LocalDateTime.now()))
                .map(it -> it.profileID);

        core.databaseHandler.removeAllExpiredOneTimeLoginData();
        return result;
    }

    public List<Integer> getAvailableProfileIDListByUserID(int userID) {
        return core.databaseHandler.getAvailableProfileIDListByUserID(userID);
    }

    public GetUserResult getOrCreateUser(String loginMethod, UUID userUUID, String username) {
        try {
            User user = core.databaseHandler.getUserByUUIDAndLoginMethod(userUUID, loginMethod);
            if (user != null) return new GetUserResult.GetUserSucceedResult(user);
            user = core.databaseHandler.createUser(userUUID, loginMethod, username);
            core.platform.getPlatformLogger().info("Created new user: " + user.getDisplayName());
            return new GetUserResult.GetUserSucceedResult(user);
        } catch (Throwable t) {
            return new GetUserResult.GetUserFailedResult(t);
        }
    }

    public record OneTimeLogin(
            int userID,
            int profileID,
            LocalDateTime expirationTime
    ) {

    }

    public record User(
            int userID,
            String loginMethod,
            UUID userUUID,
            String username,
            Optional<Integer> selectProfileID
    ) {
        public String getDisplayName() {
            return username + "(id: " + userID + ", login method: " + loginMethod + ")";
        }
    }

    public sealed abstract static class GetUserResult {
        public final static class GetUserFailedResult extends GetUserResult {
            public final Throwable throwable;

            public GetUserFailedResult(Throwable throwable) {
                this.throwable = throwable;
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
