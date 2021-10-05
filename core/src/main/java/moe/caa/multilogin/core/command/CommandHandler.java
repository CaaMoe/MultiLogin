package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.command.executes.multilogin.MultiLoginCommandExecutor;
import moe.caa.multilogin.core.command.executes.whitelist.WhitelistCommandExecutor;
import moe.caa.multilogin.core.impl.ISender;
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
        try {
            if (arguments.getLength() != 0) {
                BaseCommandExecutor executor = rootCommandExecutorConcurrentHashMap.get(arguments.getIndex(0).toLowerCase(Locale.ROOT));
                if (executor != null) {
                    if (testPermission(sender, executor.getPermission(), true)) {
                        arguments.offset(1);
                        executor.execute(sender, arguments);
                    }
                    return;
                }
            }
            sender.sendMessage(core.getLanguageHandler().getMessage("command_exception_unknown_command", FormatContent.empty()));
        } catch (Exception e) {

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

    /**
     * 测试某位执行者是否有某权限
     *
     * @param sender     命令执行者
     * @param permission 权限
     * @param feedback   没有权限时是否需要反馈
     * @return 是否具有某权限
     */
    public boolean testPermission(ISender sender, String permission, boolean feedback) {
        if (permission == null) return true;
        boolean ret = sender.hasPermission(permission);
        if (!ret) {
            sender.sendMessage(core.getLanguageHandler().getMessage("command_exception_missing_permission", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("permission").content(permission).build()
            )));
        }
        return ret;
    }
}
