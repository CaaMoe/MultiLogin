package moe.caa.multilogin.core.command.executes.multilogin.subcommands;

import moe.caa.multilogin.core.command.BaseCommandExecutor;
import moe.caa.multilogin.core.command.CommandArguments;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

public class ReloadCommand extends BaseCommandExecutor {

    /**
     * 构建这个命令处理器
     *
     * @param core 插件核心
     */
    public ReloadCommand(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, Permissions.COMMAND_MULTI_LOGIN_RELOAD);
    }

    @Override
    protected void execute(ISender sender, CommandArguments arguments) {
        if (arguments.getLength() == 0) {
            System.out.println("哼哼啊啊啊啊啊啊啊，我被reload啦");
            return;
        }
        sender.sendMessage(getCore().getLanguageHandler().getMessage("command_exception_unknown_command", FormatContent.empty()));
    }
}
