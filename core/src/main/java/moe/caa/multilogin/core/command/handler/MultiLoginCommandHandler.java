package moe.caa.multilogin.core.command.handler;

import moe.caa.multilogin.core.command.CommandManager;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Locale;

/**
 * 命令 '/MultiLogin ...' 综合处理程序
 */
public class MultiLoginCommandHandler extends AbstractHandler {

    /**
     * 构建这个命令处理程序
     *
     * @param commandManager 命令管理器
     * @param name           根命令名称
     */
    public MultiLoginCommandHandler(CommandManager commandManager, String name) {
        super(commandManager, name);
    }

    @Override
    public boolean execute(ISender sender, String[] args) {
        if (args.length == 1) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload":
                    executeReload(sender);
                    return true;
                case "update":
                    executeUpdate(sender);
                    return true;
                case "list":
                    executeList(sender);
                    return true;
                case "confirm":
                    executeConfirm(sender);
                    return true;
            }
        }
        return false;

    }

    /**
     * 命令 'MultiLogin reload'
     *
     * @param sender 命令执行者
     */
    private void executeReload(ISender sender) {
        if (hasPermission(sender, Permissions.COMMAND_RELOAD)) {
            getCommandManager().getCore().reload();
            sender.sendMessage(getCommandManager().getCore().getLanguageHandler().getMessage("command_reloaded", FormatContent.empty()));
        }
    }

    /**
     * 命令 'MultiLogin update'
     *
     * @param sender 命令执行者
     */
    private void executeUpdate(ISender sender) {

    }

    /**
     * 命令 'MultiLogin list'
     *
     * @param sender 命令执行者
     */
    private void executeList(ISender sender) {

    }

    /**
     * 命令 'MultiLogin confirm'
     *
     * @param sender 命令执行者
     */
    private void executeConfirm(ISender sender) {

    }
}
