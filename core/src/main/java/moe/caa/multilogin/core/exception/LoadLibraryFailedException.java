package moe.caa.multilogin.core.exception;

/**
 * 依赖加载异常
 */
public class LoadLibraryFailedException extends Exception {
    public LoadLibraryFailedException() {
        super();
    }

    public LoadLibraryFailedException(String s) {
        super(s);
    }

    public LoadLibraryFailedException(Throwable t) {
        super(t);
    }
}
