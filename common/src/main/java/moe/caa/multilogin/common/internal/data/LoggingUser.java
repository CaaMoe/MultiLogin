package moe.caa.multilogin.common.internal.data;

import moe.caa.multilogin.common.internal.util.Key;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface LoggingUser {

    UUID getExpectUUID();

    String getExpectUsername();

    InetAddress getPlayerIP();

    boolean isTransferred();

    byte[] requestCookie(Key cookieKey) throws ExecutionException, InterruptedException, Exception;

    SwitchToEncryptedResult switchToEncryptedState(boolean shouldClientAuthenticate) throws ExecutionException, InterruptedException, Exception;

    void closeConnect(Component disconnect);

    void closeConnection();

    void completeLogin(OnlineData data) throws Throwable;


    abstract sealed class SwitchToEncryptedResult {
        public static final class SwitchToEncryptedSucceedResult extends SwitchToEncryptedResult {
            public final String serverID;

            public SwitchToEncryptedSucceedResult(String serverID) {
                this.serverID = serverID;
            }
        }

        abstract static sealed public class SwitchToEncryptedFailedResult extends SwitchToEncryptedResult {
            public static final class SwitchToEncryptedFailedThrowResult extends SwitchToEncryptedFailedResult {
                public final Throwable throwable;

                public SwitchToEncryptedFailedThrowResult(Throwable throwable) {
                    this.throwable = throwable;
                }
            }

            public static final class SwitchToEncryptedFailedReasonResult extends SwitchToEncryptedFailedResult {
                public final Reason cause;

                public SwitchToEncryptedFailedReasonResult(Reason cause) {
                    this.cause = cause;
                }

                public enum Reason {
                    CLOSED,
                    CRYPT_ERROR
                }
            }
        }
    }
}
