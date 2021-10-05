package moe.caa.multilogin.core.command;

import lombok.AccessLevel;
import lombok.Getter;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 代表一个最顶级的命令处理器
 */
@Getter(value = AccessLevel.PROTECTED)
public abstract class BaseCommandExecutor {
    private final MultiCore core;
    private final CommandHandler commandHandler;
    private final String permission;
    private final ConcurrentHashMap<String, BaseCommandExecutor> subCommand = new ConcurrentHashMap<>();

    protected BaseCommandExecutor(MultiCore core, CommandHandler commandHandler, String permission) {
        this.core = core;
        this.commandHandler = commandHandler;
        this.permission = permission;
    }

    /**
     * 执行这条指令
     *
     * @param sender    命令执行者
     * @param arguments 命令参数
     */
    protected CommandResult execute(ISender sender, CommandArguments arguments) throws SQLException, ExecutionException, InterruptedException {
        if (arguments.getLength() != 0) {
            BaseCommandExecutor executor = subCommand.get(arguments.getIndex(0).toLowerCase(Locale.ROOT));
            if (executor != null) {
                if (executor.getPermission() == null || sender.hasPermission(executor.getPermission())) {
                    arguments.offset(1);
                    return executor.execute(sender, arguments);
                }
                return CommandResult.NO_PERMISSION;
            }
        }
        return CommandResult.UNKNOWN_USAGE;
    }

    /**
     * 补全这条指令
     *
     * @param sender    命令执行者
     * @param arguments 命令参数
     */
    protected List<String> tabComplete(ISender sender, CommandArguments arguments) {
        return Collections.emptyList();
    }
}
