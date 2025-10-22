package moe.caa.multilogin.common.internal.profile;

import kotlin.collections.ArraysKt;
import moe.caa.multilogin.common.internal.main.MultiCore;

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
                    case RANDOM -> createProfile(UUID.randomUUID(), expectName, amendRuleUUID, amendRuleName);
                    case FAIL ->
                            new CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason(CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason.Reason.UUID_ALREADY_EXISTS);
                }
            }
            // name 重复处理
            if (core.databaseHandler.getProfileByName(expectName) != null) {
                switch (amendRuleName) {
                    case INCREMENT_NUMBER_AND_LEFT_TRUNCATE, INCREMENT_NUMBER_AND_RIGHT_TRUNCATE, INCREMENT_NUMBER -> {
                        String amended = amendRuleName.amend(expectName);
                        if (amended.length() > MAX_PROFILE_NAME_LENGTH) {
                            return new CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason(CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason.Reason.NAME_AMEND_TOO_LONG);
                        }
                        return createProfile(expectUUID, amended, amendRuleUUID, amendRuleName);
                    }
                    case FAIL ->
                            new CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason(CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureReason.Reason.NAME_ALREADY_EXISTS);
                }
            }

            return new CreateProfileResult.CreateProfileResultSuccess(
                    core.databaseHandler.createProfile(expectUUID, expectName)
            );
        } catch (Throwable throwable) {
            return new CreateProfileResult.CreateProfileResultFailure.CreateProfileResultFailureThrow(throwable);
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

    }

    public sealed static class CreateProfileResult {
        public sealed static class CreateProfileResultFailure extends CreateProfileResult {
            public final static class CreateProfileResultFailureReason extends CreateProfileResultFailure {
                public final Reason reason;

                public CreateProfileResultFailureReason(Reason reason) {
                    this.reason = reason;
                }

                public enum Reason {
                    UUID_ALREADY_EXISTS,
                    NAME_ALREADY_EXISTS,
                    NAME_AMEND_TOO_LONG;
                }
            }

            public final static class CreateProfileResultFailureThrow extends CreateProfileResultFailure {
                public final Throwable throwable;

                public CreateProfileResultFailureThrow(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }

        public final static class CreateProfileResultSuccess extends CreateProfileResult {
            public final Profile profile;

            public CreateProfileResultSuccess(Profile profile) {
                this.profile = profile;
            }
        }
    }
}
