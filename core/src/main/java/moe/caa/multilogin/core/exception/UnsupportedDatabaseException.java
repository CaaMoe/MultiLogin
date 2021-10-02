package moe.caa.multilogin.core.exception;

import lombok.NoArgsConstructor;

/**
 * 未知的支持数据库类型异常
 */
@NoArgsConstructor
public class UnsupportedDatabaseException extends Exception {

    /**
     * 构建这个异常
     *
     * @param message 异常信息
     */
    public UnsupportedDatabaseException(String message) {
        super(message);
    }
}
