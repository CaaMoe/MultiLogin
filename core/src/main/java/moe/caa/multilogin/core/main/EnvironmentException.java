package moe.caa.multilogin.core.main;

/**
 * 运行环境异常
 */
public class EnvironmentException extends RuntimeException {
    public EnvironmentException() {
    }

    public EnvironmentException(String message) {
        super(message);
    }
}
