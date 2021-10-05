package moe.caa.multilogin.core.command;

/**
 * 命令执行结果
 */
public enum CommandResult {

    /**
     * 执行成功
     */
    PASS,

    /**
     * 没有权限
     */
    NO_PERMISSION,

    /**
     * 未知用法
     */
    UNKNOWN_USAGE;
}
