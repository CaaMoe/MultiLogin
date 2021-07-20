package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;

public class WhitelistCommand {

    private final MultiCore core;

    public WhitelistCommand(MultiCore core) {
        this.core = core;
    }

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                //根命令和权限
                CommandHandler.literal("whitelist").requires(Permission.MULTI_LOGIN_WHITELIST::hasPermission)
                        //二级命令
                        .then(CommandHandler.literal("add")
                                //需求字符串参数
                                .then(CommandHandler.argument("target", StringArgumentType.string())
                                        //执行
                                        .executes(this::executeAdd)
                                )
                        )
                        .then(CommandHandler.literal("remove")
                                .then(CommandHandler.argument("target", StringArgumentType.string())
                                        .executes(this::executeRemove)
                                )
                        )
        );
    }

    private int executeAdd(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String arg = StringArgumentType.getString(context, "target");
                boolean b = core.getSqlManager().getCacheWhitelistDataHandler().addCacheWhitelist(arg);
                if (b) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD.getMessage(core, arg)));
                } else {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD_ALREADY.getMessage(core, arg)));
                }
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }

    private int executeRemove(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String arg = StringArgumentType.getString(context, "target");
                boolean b = core.getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(arg);
                if (b) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(core, arg)));
                } else {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(core, arg)));
                }
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }
}
