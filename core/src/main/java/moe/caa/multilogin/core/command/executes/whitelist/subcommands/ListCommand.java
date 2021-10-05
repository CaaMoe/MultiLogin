package moe.caa.multilogin.core.command.executes.whitelist.subcommands;

import moe.caa.multilogin.core.command.*;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 命令 '/whitelist list' 处理程序
 */
public class ListCommand extends BaseCommandExecutor {
    public ListCommand(CommandHandler commandHandler, MultiCore core) {
        super(core, commandHandler, Permissions.COMMAND_WHITELIST_LIST);
    }

    @Override
    protected CommandResult execute(ISender sender, CommandArguments arguments) throws SQLException, ExecutionException, InterruptedException {
        if (arguments.getLength() == 0) {
            List<User> users = getCore().getSqlManager().getUserDataHandler().getUserEntryWhereHaveWhitelist();
            List<String> cacheWhitelist = getCore().getSqlManager().getCacheWhitelistDataHandler().getAllCacheWhitelist();
            int count = users.size() + cacheWhitelist.size();
            if (count == 0) {
                sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_list_empty", FormatContent.empty()));
                return CommandResult.PASS;
            }
            StringBuilder sb = new StringBuilder();
            for (String s : cacheWhitelist) {
                sb.append("§7*").append(s).append("*").append(", ");
            }
            if (cacheWhitelist.size() != 0 && users.size() == 0) sb.setLength(sb.length() - 2);
            for (User user : users) {
                FutureTask<Boolean> task = new FutureTask<>(() -> getCore().getPlugin().getRunServer().getPlayerManager().hasOnline(user.getRedirectUuid()));
                getCore().getPlugin().getRunServer().getScheduler().runTask(task);
                boolean b = task.get();
                sb.append('§').append(b ? 'a' : '7');
                sb.append(user.getCurrentName()).append(", ");
            }
            if (users.size() != 0) sb.setLength(sb.length() - 2);
            sender.sendMessage(getCore().getLanguageHandler().getMessage("command_message_whitelist_list", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("count").content(count).build(),
                    FormatContent.FormatEntry.builder().name("cache_count").content(cacheWhitelist.size()).build(),
                    FormatContent.FormatEntry.builder().name("list").content(sb).build()
            )));
            return CommandResult.PASS;
        }
        return CommandResult.UNKNOWN_USAGE;
    }
}
