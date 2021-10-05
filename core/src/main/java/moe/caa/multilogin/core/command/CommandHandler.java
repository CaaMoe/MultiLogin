package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.command.executes.multilogin.MultiLoginCommandExecutor;
import moe.caa.multilogin.core.command.executes.whitelist.WhitelistCommandExecutor;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令处理程序
 */
public class CommandHandler {
    private final MultiCore core;
    private final ConcurrentHashMap<String, BaseCommandExecutor> rootCommandExecutorConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 构建这个命令处理程序
     *
     * @param core 插件核心
     */
    public CommandHandler(MultiCore core) {
        this.core = core;
        rootCommandExecutorConcurrentHashMap.put("multilogin", new MultiLoginCommandExecutor(this, core));
        rootCommandExecutorConcurrentHashMap.put("whitelist", new WhitelistCommandExecutor(this, core));
    }

    /**
     * 异步执行这条指令
     *
     * @param sender    命令执行者
     * @param arguments 命令参数
     */
    public void executeAsync(ISender sender, CommandArguments arguments) {
        core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> execute(sender, arguments));
    }

    /**
     * 执行这条指令
     *
     * @param sender    命令执行者
     * @param arguments 命令参数
     */
    public void execute(ISender sender, CommandArguments arguments) {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.join("Executing command: %s. (%s)", arguments.toString(), sender.getName()));
        try {
            CommandResult result = CommandResult.UNKNOWN_USAGE;
            if (arguments.getLength() != 0) {
                BaseCommandExecutor executor = rootCommandExecutorConcurrentHashMap.get(arguments.getIndex(0).toLowerCase(Locale.ROOT));
                if (executor != null) {
                    if(executor.getPermission() == null || sender.hasPermission(executor.getPermission())){
                        arguments.offset(1);
                        result = executor.execute(sender, arguments);
                    } else {
                        result = CommandResult.NO_PERMISSION;
                    }
                }
            }
            if(result == CommandResult.PASS) return;
            if(result == CommandResult.UNKNOWN_USAGE) {
                sender.sendMessage(core.getLanguageHandler().getMessage("command_exception_unknown_command", FormatContent.empty()));
            } else if (result == CommandResult.NO_PERMISSION){
                sender.sendMessage(core.getLanguageHandler().getMessage("command_exception_missing_permission", FormatContent.empty()));
            }
        } catch (Exception e) {
            sender.sendMessage(core.getLanguageHandler().getMessage("command_error", FormatContent.empty()));
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while executing the command.", e);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "sender: " + sender.getName());
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "arguments: " + arguments);
        }
    }

    /**
     * 补全这条指令
     *
     * @param sender    命令执行者
     * @param arguments 命令参数
     */
    public List<String> tabComplete(ISender sender, CommandArguments arguments) {
        return Collections.emptyList();
    }
}
