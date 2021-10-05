package moe.caa.multilogin.core.command.executes.whitelist;

import moe.caa.multilogin.core.command.BaseCommandExecutor;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.executes.whitelist.subcommands.AddCommand;
import moe.caa.multilogin.core.command.executes.whitelist.subcommands.ListCommand;
import moe.caa.multilogin.core.command.executes.whitelist.subcommands.RemoveCommand;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 命令 '/whitelist [args...] 处理程序'
 */
public class WhitelistCommandExecutor extends BaseCommandExecutor {
    /**
     * 构建这个命令处理器
     *
     * @param core 插件核心
     */
    public WhitelistCommandExecutor(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, null);
        getSubCommand().put("add", new AddCommand(commandHandler, core));
        getSubCommand().put("remove", new RemoveCommand(commandHandler, core));
        getSubCommand().put("list", new ListCommand(commandHandler, core));
    }
}
