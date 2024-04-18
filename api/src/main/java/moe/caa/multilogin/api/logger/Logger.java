package moe.caa.multilogin.api.logger;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class Logger {
    private void handleLog0(Level level, String message, Throwable throwable){
        if (level == Level.DEBUG && LoggerProvider.debugMode) {
            level = Level.INFO;
        }
        handleLog(level, message, throwable);
    }

    protected abstract void handleLog(Level level, String message, Throwable throwable);
    public void log(Level level, String message, Throwable throwable){handleLog0(level, message, throwable);}
    public void log(Level level, String message) {handleLog0(level, message, null);}
    public void log(Level level, Throwable throwable) {handleLog0(level, null, throwable);}
    public void debug(String message, Throwable throwable) {handleLog0(Level.DEBUG, message, throwable);}
    public void debug(String message) {log(Level.DEBUG, message);}
    public void debug(Throwable throwable) {handleLog0(Level.DEBUG, null, throwable);}
    public void info(String message, Throwable throwable) {handleLog0(Level.INFO, message, throwable);}
    public void info(String message) {log(Level.INFO, message);}
    public void info(Throwable throwable) {handleLog0(Level.INFO, null, throwable);}
    public void warn(String message, Throwable throwable) {handleLog0(Level.WARN, message, throwable);}
    public void warn(String message) {log(Level.WARN, message);}
    public void warn(Throwable throwable) {handleLog0(Level.WARN, null, throwable);}
    public void error(String message, Throwable throwable) {handleLog0(Level.ERROR, message, throwable);}
    public void error(String message) {log(Level.ERROR, message);}
    public void error(Throwable throwable) {handleLog0(Level.ERROR, null, throwable);}
}