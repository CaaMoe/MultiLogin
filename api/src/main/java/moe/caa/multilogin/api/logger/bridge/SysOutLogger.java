package moe.caa.multilogin.api.logger.bridge;

import moe.caa.multilogin.api.logger.Level;
import moe.caa.multilogin.api.logger.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SysOutLogger extends Logger {
    @Override
    public void handleLog(Level level, String message, Throwable throwable) {
        if(throwable != null){
            switch (level){
                case DEBUG, INFO -> throwable.printStackTrace(System.out);
                default -> throwable.printStackTrace(System.err);
            }
        }
        if(message != null){
            System.out.printf("[%s] %s", level.name(), message);
        }
    }
}
