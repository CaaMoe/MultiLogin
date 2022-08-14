package moe.caa.multilogin.core.skinrestorer;

import java.io.IOException;

/**
 * 皮肤修复异常
 */
public class SkinRestorerException extends IOException {
    public SkinRestorerException() {
    }

    public SkinRestorerException(String message) {
        super(message);
    }

    public SkinRestorerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkinRestorerException(Throwable cause) {
        super(cause);
    }
}
