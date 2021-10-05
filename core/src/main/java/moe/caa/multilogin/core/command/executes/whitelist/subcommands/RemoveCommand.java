package moe.caa.multilogin.core.command.executes.whitelist.subcommands;

import moe.caa.multilogin.core.command.BaseCommandExecutor;
import moe.caa.multilogin.core.command.CommandArguments;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;

import java.sql.SQLException;
import java.util.UUID;

/**
 * 命令 '/whitelist remove [args...]' 处理程序
 */
public class RemoveCommand extends BaseCommandExecutor {
    public RemoveCommand(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, Permissions.COMMAND_WHITELIST_REMOVE);
    }

    @Override
    protected void execute(ISender sender, CommandArguments arguments) throws SQLException {
        if (arguments.getLength() == 1) {
            boolean result = false;
            int count = 0;
            if (getCore().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(arguments.getIndex(0))) {
                result = true;
                count++;
                getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                    getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(arguments.getIndex(0),
                            getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                });
            }
            for (User user : getCore().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(arguments.getIndex(0))) {
                if (user.isWhitelist()) {
                    result = true;
                    user.setWhitelist(false);
                    getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
                    count++;
                    getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                        getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(user.getRedirectUuid(),
                                getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                    });
                }
            }
            UUID whenUuid = ValueUtil.getUuidOrNull(arguments.getIndex(0));
            if (whenUuid != null) {
                for (User user : getCore().getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(whenUuid)) {
                    if (user.isWhitelist()) {
                        result = true;
                        user.setWhitelist(false);
                        getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
                        count++;
                        getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                            getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(user.getRedirectUuid(),
                                    getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                        });
                    }
                }
                User whenOnlineUuidUser = getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(whenUuid);
                if (whenOnlineUuidUser != null) {
                    if (whenOnlineUuidUser.isWhitelist()) {
                        whenOnlineUuidUser.setWhitelist(false);
                        getCore().getSqlManager().getUserDataHandler().updateUserEntry(whenOnlineUuidUser);
                        count++;
                        getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                            getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(whenOnlineUuidUser.getRedirectUuid(),
                                    getCore().getLanguageHandler().getMessage("in_game_whitelist_removed", FormatContent.empty()));
                        });
                    }
                }
            }

            if (result) {
                sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_removed", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name_or_uuid").content(arguments.getIndex(0)).build(),
                        FormatContent.FormatEntry.builder().name("count").content(count).build()
                )));
            } else {
                sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name_or_uuid").content(arguments.getIndex(0)).build()
                )));
            }
            return;
        }
        sender.sendMessage(getCore().getLanguageHandler().getMessage("command_exception_unknown_command", FormatContent.empty()));
    }
}
