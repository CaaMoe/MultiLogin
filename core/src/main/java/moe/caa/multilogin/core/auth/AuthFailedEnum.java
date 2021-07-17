package moe.caa.multilogin.core.auth;

public enum AuthFailedEnum {

    /**
     * 由于宕机或无法连接
     */
    SERVER_DOWN,

    /**
     * 身份验证失败
     */
    VALIDATION_FAILED;
}