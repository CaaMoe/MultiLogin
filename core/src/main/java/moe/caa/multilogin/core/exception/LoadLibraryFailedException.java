package moe.caa.multilogin.core.exception;

import lombok.NoArgsConstructor;

/**
 * 依赖加载异常
 */
@NoArgsConstructor
public class LoadLibraryFailedException extends Exception {

    /**
     * 构建这个异常
     *
     * @param s 异常信息
     */
    public LoadLibraryFailedException(String s) {
        super(s);
    }

    /**
     * 构建这个异常
     *
     * @param t 异常栈
     */
    public LoadLibraryFailedException(Throwable t) {
        super(t);
    }
}
