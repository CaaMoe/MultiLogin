package moe.caa.multilogin.core.auth.yggdrasil;

/**
 * Yggdrasil 账户验证返回原因枚举
 */
public enum YggdrasilAuthReasonEnum {

    /**
     * 访问没有错误的返回
     */
    RETURN,

    /**
     * 由于宕机或无法连接
     */
    SERVER_DOWN,

    /**
     * 身份验证失败
     */
    VALIDATION_FAILED,

    /**
     * 由于配置失误没有匹配到验证服务器
     */
    NO_SERVICE,

    /**
     * 异常
     */
    ERROR;
}