package moe.caa.multilogin.common.internal.service;

import moe.caa.multilogin.common.internal.data.GameProfile;

import java.net.InetAddress;

public interface LocalYggdrasilSessionService {

    HasJoinedResult hasJoined(String serverID, String username, InetAddress playerIP);

    sealed abstract class HasJoinedResult {
        public static final class HasJoinedSucceedResult extends HasJoinedResult {
            public final GameProfile profile;

            public HasJoinedSucceedResult(GameProfile profile) {
                this.profile = profile;
            }
        }

        public static sealed abstract class HasJoinedFailedResult extends HasJoinedResult {
            public static final class HasJoinedFailedInvalidSessionResult extends HasJoinedFailedResult {
            }

            public static final class hasJoinedFailedServiceUnavailableResult extends HasJoinedFailedResult {
                public final Throwable throwable;

                public hasJoinedFailedServiceUnavailableResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }

            public static final class HasJoinedFailedThrowResult extends HasJoinedFailedResult {
                public final Throwable throwable;

                public HasJoinedFailedThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }
        }
    }
}
