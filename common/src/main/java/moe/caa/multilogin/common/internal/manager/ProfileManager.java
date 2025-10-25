package moe.caa.multilogin.common.internal.manager;

import kotlin.collections.ArraysKt;
import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig;
import moe.caa.multilogin.common.internal.data.Profile;
import moe.caa.multilogin.common.internal.data.User;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.util.StringUtil;

import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Function;

public class ProfileManager {
    private static final int MAX_PROFILE_NAME_LENGTH = 16;
    private final MultiCore core;

    public ProfileManager(MultiCore core) {
        this.core = core;
    }


    public CreateProfileResult createProfile(AuthenticationConfig config, User ownedUser, int putProfileSlot) {
        return createProfile(
                ownedUser.userID,
                config.uuidConflictPolicy.get(),
                config.nameConflictPolicy.get(),
                ownedUser.userUUID,
                ownedUser.username,
                putProfileSlot
        );
    }

    public CreateProfileResult createProfile(
            int ownedUserID,
            UUIDConflictPolicy uuidConflictPolicy,
            NameConflictPolicy nameConflictPolicy,
            UUID expectUUID,
            String expectName,
            int putProfileSlot
    ) {
        try {
            // uuid 重复处理
            if (core.databaseHandler.getProfileByProfileUUID(expectUUID) != null) {
                switch (uuidConflictPolicy) {
                    case RANDOM -> {
                        UUID amended = UUID.randomUUID();
                        core.platform.getPlatformLogger().warn("Create profile UUID conflict detected, amended from " + expectUUID + " to " + amended + " using role: " + StringUtil.underscoreUpperCaseToKebabCase(uuidConflictPolicy.name()));
                        return createProfile(ownedUserID, uuidConflictPolicy, nameConflictPolicy, amended, expectName, putProfileSlot);
                    }
                    case REJECT ->
                            new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(
                                    CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.UUID_CONFLICT);
                }
            }
            // name 重复处理
            if (core.databaseHandler.getProfileByProfileName(expectName) != null) {
                switch (nameConflictPolicy) {
                    case INCREMENT_LEFT_TRUNCATE, INCREMENT_RIGHT_TRUNCATE, INCREMENT -> {
                        String amended = nameConflictPolicy.amend(expectName);
                        if (amended.length() > MAX_PROFILE_NAME_LENGTH) {
                            return new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.NAME_AMEND_RESTRICT);
                        }
                        core.platform.getPlatformLogger().warn("Create profile name conflict detected, amended from " + expectName + " to " + amended + " using role: " + StringUtil.underscoreUpperCaseToKebabCase(nameConflictPolicy.name()));
                        return createProfile(ownedUserID, uuidConflictPolicy, nameConflictPolicy, expectUUID, amended, putProfileSlot);
                    }
                    case REJECT -> {
                        return new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(
                                CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.NAME_CONFLICT);
                    }
                }
            }

            Profile profile = core.databaseHandler.createProfile(expectUUID, expectName, ownedUserID, putProfileSlot);
            core.platform.getPlatformLogger().info("Created new profile: " + profile.displayName());
            return new CreateProfileResult.CreateProfileSucceedResult(profile);
        } catch (Throwable throwable) {
            return new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult(throwable);
        }
    }

    public enum UUIDConflictPolicy {
        REJECT,
        RANDOM;
    }

    public enum NameConflictPolicy {
        REJECT(name -> name),
        INCREMENT(name -> {
            StringBuilder numberSpec = new StringBuilder();
            for (char c : ArraysKt.reversedArray(name.toCharArray())) {
                if (Character.isDigit(c)) {
                    numberSpec.insert(0, c);
                }
            }
            String nameSpec = name.substring(0, name.length() - numberSpec.length());
            if (numberSpec.isEmpty()) numberSpec.append("0");

            numberSpec = new StringBuilder((new BigInteger(numberSpec.toString()).add(BigInteger.ONE)).toString());

            return nameSpec + numberSpec;
        }),
        INCREMENT_LEFT_TRUNCATE(name -> {
            name = INCREMENT.amend(name);
            if (name.length() <= MAX_PROFILE_NAME_LENGTH) {
                return name;
            }

            StringBuilder numberSpec = new StringBuilder();
            for (char c : name.toCharArray()) {
                if (Character.isDigit(c)) {
                    numberSpec.insert(0, c);
                }
            }
            String nameSpec = name.substring(0, name.length() - numberSpec.length());
            if (nameSpec.isEmpty()) {
                return name;
            }
            return nameSpec.substring(1) + numberSpec;
        }),
        INCREMENT_RIGHT_TRUNCATE(name -> {
            name = INCREMENT.amend(name);
            if (name.length() <= MAX_PROFILE_NAME_LENGTH) {
                return name;
            }

            StringBuilder numberSpec = new StringBuilder();
            for (char c : name.toCharArray()) {
                if (Character.isDigit(c)) {
                    numberSpec.insert(0, c);
                }
            }
            String nameSpec = name.substring(0, name.length() - numberSpec.length());
            if (nameSpec.isEmpty()) {
                return name;
            }
            return nameSpec.substring(0, nameSpec.length() - 1) + numberSpec;
        });

        private final Function<String, String> amendFunction;

        NameConflictPolicy(Function<String, String> amendFunction) {
            this.amendFunction = amendFunction;
        }

        public String amend(String name) {
            return amendFunction.apply(name);
        }
    }

    public sealed abstract static class CreateProfileResult {

        public sealed static abstract class CreateProfileFailedResult extends CreateProfileResult {
            public final static class CreateProfileFailedBecauseReasonResult extends CreateProfileFailedResult {
                public final Reason reason;

                CreateProfileFailedBecauseReasonResult(Reason reason) {
                    this.reason = reason;
                }

                public enum Reason {
                    UUID_CONFLICT,
                    NAME_CONFLICT,
                    NAME_AMEND_RESTRICT;
                }
            }

            public final static class CreateProfileFailedBecauseThrowResult extends CreateProfileFailedResult {
                public final Throwable throwable;

                CreateProfileFailedBecauseThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }

        public final static class CreateProfileSucceedResult extends CreateProfileResult {
            public final Profile profile;

            CreateProfileSucceedResult(Profile profile) {
                this.profile = profile;
            }
        }
    }
}
