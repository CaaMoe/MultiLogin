package moe.caa.multilogin.common.internal.manager;

import kotlin.collections.ArraysKt;
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

    public Profile getProfileSnapshotByID(int profileID) {
        return core.databaseHandler.getProfileByID(profileID);
    }

    public CreateProfileResult createProfile(UUID expectUUID, String expectName, AmendRuleUUID amendRuleUUID, AmendRuleName amendRuleName) {
        try {
            // uuid 重复处理
            if (core.databaseHandler.getProfileByUUID(expectUUID) != null) {
                switch (amendRuleUUID) {
                    case RANDOM -> {
                        UUID amended = UUID.randomUUID();
                        core.platform.getPlatformLogger().warn("Create profile UUID conflict detected, amended from " + expectUUID + " to " + amended + " using role: " + StringUtil.underscoreUpperCaseToKebabCase(amendRuleUUID.name()));
                        createProfile(amended, expectName, amendRuleUUID, amendRuleName);
                    }
                    case FAIL ->
                            new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(
                                    CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.UUID_CONFLICT);
                }
            }
            // name 重复处理
            if (core.databaseHandler.getProfileByName(expectName) != null) {
                switch (amendRuleName) {
                    case INCREMENT_NUMBER_AND_LEFT_TRUNCATE, INCREMENT_NUMBER_AND_RIGHT_TRUNCATE, INCREMENT_NUMBER -> {
                        String amended = amendRuleName.amend(expectName);
                        if (amended.length() > MAX_PROFILE_NAME_LENGTH) {
                            return new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.NAME_AMEND_RESTRICT);
                        }
                        core.platform.getPlatformLogger().warn("Create profile name conflict detected, amended from " + expectName + " to " + amended + " using role: " + StringUtil.underscoreUpperCaseToKebabCase(amendRuleName.name()));
                        return createProfile(expectUUID, amended, amendRuleUUID, amendRuleName);
                    }
                    case FAIL ->
                            new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult(
                                    CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult.Reason.NAME_CONFLICT);
                }
            }

            Profile profile = core.databaseHandler.createProfile(expectUUID, expectName);
            core.platform.getPlatformLogger().info("Created new profile: " + profile.getDisplayName());
            return new CreateProfileResult.CreateProfileSucceedResult(profile);
        } catch (Throwable throwable) {
            return new CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult(throwable);
        }
    }

    public enum AmendRuleUUID {
        FAIL,
        RANDOM;
    }

    public enum AmendRuleName {
        FAIL(name -> name),
        INCREMENT_NUMBER(name -> {
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
        INCREMENT_NUMBER_AND_LEFT_TRUNCATE(name -> {
            name = INCREMENT_NUMBER.amend(name);
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
        INCREMENT_NUMBER_AND_RIGHT_TRUNCATE(name -> {
            name = INCREMENT_NUMBER.amend(name);
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

        AmendRuleName(Function<String, String> amendFunction) {
            this.amendFunction = amendFunction;
        }

        public String amend(String name) {
            return amendFunction.apply(name);
        }
    }

    public record Profile(
            int profileID,
            UUID profileUUID,
            String profileName
    ) {
        public String getDisplayName() {
            return profileName + "(id: " + profileID + ", uuid: " + profileUUID + ")";
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
