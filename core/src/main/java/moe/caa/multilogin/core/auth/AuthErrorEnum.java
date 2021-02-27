package moe.caa.multilogin.core.auth;

/**
 * Yggdrasil验证失败枚举
 */
public enum AuthErrorEnum {

    /**
     * 由于宕机或无法连接
     */
    SERVER_DOWN,

    /**
     * 身份验证失败
     */
    VALIDATION_FAILED,
}
