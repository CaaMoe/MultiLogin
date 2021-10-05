package moe.caa.multilogin.core.command.executes.multilogin;

import moe.caa.multilogin.core.command.BaseCommandExecutor;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.executes.multilogin.subcommands.ReloadCommand;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 命令 '/multilogin [args...] 处理程序'
 */
public class MultiLoginCommandExecutor extends BaseCommandExecutor {

    /**
     * 构建这个命令处理器
     *
     * @param core 插件核心
     */
    public MultiLoginCommandExecutor(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, null);
        getSubCommand().put("reload", new ReloadCommand(commandHandler, core));
    }
}
