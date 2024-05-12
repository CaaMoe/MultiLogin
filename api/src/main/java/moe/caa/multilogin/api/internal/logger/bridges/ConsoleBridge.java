package moe.caa.multilogin.api.internal.logger.bridges;

import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * 控制台日志程序桥接
 */
@ApiStatus.Internal
@NoArgsConstructor
public class ConsoleBridge extends BaseLoggerBridge {


    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level == Level.DEBUG) {
            System.out.println("[DEBUG] " + message);
            if (throwable != null)
                throwable.printStackTrace(System.out);
        } else if (level == Level.INFO) {
            System.out.println("[INFO] " + message);
            if (throwable != null)
                throwable.printStackTrace(System.out);
        } else if (level == Level.WARN) {
            System.out.println("[WARN] " + message);
            if (throwable != null)
                throwable.printStackTrace(System.err);
        } else if (level == Level.ERROR) {
            System.out.println("[ERROR] " + message);
            if (throwable != null)
                throwable.printStackTrace(System.err);
        }
    }
}
