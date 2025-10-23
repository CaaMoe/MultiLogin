package moe.caa.multilogin.common.internal.manager;

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

    public Optional<Integer> getAndRemoveOneTimeLoginProfileIDByUserID(int userID) {
        Optional<Integer> result = Optional.ofNullable(core.databaseHandler.getAndRemoveOneTimeLoginDataByUserID(userID))
                .filter(oneTimeLogin -> oneTimeLogin.expirationTime.isAfter(LocalDateTime.now()))
                .map(it -> it.profileID);

        core.databaseHandler.removeAllExpiredOneTimeLoginData();
        return result;
    }

    public List<Integer> getAvailableProfileIDListByUserID(int userID) {
        return core.databaseHandler.getAvailableProfileIDListByUserID(userID);
    }

    public void removeUserSelectedProfileID(int userID) {
        core.databaseHandler.removeUserSelectedProfileID(userID);
    }

    public void setUserSelectedProfileID(int userID, int profileID) {
        core.databaseHandler.setUserSelectedProfileID(userID, profileID);
    }

    public void addUserHaveProfile(int userID, int profileID) {
        core.databaseHandler.addUserHaveProfile(userID, profileID);
    }

    public User getOrCreateUser(String loginMethod, UUID userUUID, String username) {
        User user = core.databaseHandler.getUserByUUIDAndLoginMethod(userUUID, loginMethod);
        if (user == null) {
            user = core.databaseHandler.createUser(userUUID, loginMethod, username);
            core.platform.getPlatformLogger().info("Created new user: " + user.getDisplayName());
        }
        return user;
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
}
