package moe.caa.multilogin.core.auth.verify;

/**
 * 代表身份核查结果
 */
public enum VerifyResultEnum {

    /**
     * 通过
     */
    PASS,

    /**
     * 不通过
     */
    FAIL,

    /**
     * 异常
     */
    ERROR,
}
