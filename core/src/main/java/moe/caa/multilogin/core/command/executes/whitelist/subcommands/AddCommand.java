package moe.caa.multilogin.core.command.executes.whitelist.subcommands;

import moe.caa.multilogin.core.command.*;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

import java.sql.SQLException;

/**
 * 命令 '/whitelist add [args...]' 处理程序
 */
public class AddCommand extends BaseCommandExecutor {
    public AddCommand(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, Permissions.COMMAND_WHITELIST_ADD);
    }

    @Override
    protected CommandResult execute(ISender sender, CommandArguments arguments) throws SQLException {
        if (arguments.getLength() == 1) {
            if (getCore().getSqlManager().getCacheWhitelistDataHandler().addCacheWhitelist(arguments.getIndex(0))) {
                sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_added", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name_or_uuid").content(arguments.getIndex(0)).build()
                )));
            } else {
                sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name_or_uuid").content(arguments.getIndex(0)).build()
                )));
            }
            return CommandResult.PASS;
        }
        return CommandResult.UNKNOWN_USAGE;
    }
}
