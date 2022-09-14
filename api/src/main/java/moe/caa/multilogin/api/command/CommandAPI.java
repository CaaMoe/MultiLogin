package moe.caa.multilogin.api.command;

import moe.caa.multilogin.api.plugin.ISender;

import java.util.List;

/**
 * 命令处理程序
 */
public interface CommandAPI {

    /**
     * 执行一条指令
     *
     * @param sender 指令发送者
     * @param args   指令参数
     */
    void execute(ISender sender, String[] args);

    void execute(ISender sender, String args);

    /**
     * 执行指令建议补全
     *
     * @param sender 指令发送者
     * @param args   指令参数
     */
    List<String> tabComplete(ISender sender, String[] args);

    List<String> tabComplete(ISender sender, String args);
}
