package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;

public class WhitelistCommand {

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                CommandHandler.literal("whitelist").requires(Permission.MULTI_LOGIN_WHITELIST::hasPermission)
                        .then(CommandHandler.literal("add")
                                .then(CommandHandler.argument("target", StringArgumentType.string())
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
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String arg = StringArgumentType.getString(context, "target");
                boolean b = CacheWhitelistDataHandler.addCacheWhitelist(arg);
                if (b) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD.getMessage(arg)));
                } else {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD_ALREADY.getMessage(arg)));
                }
            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }

    private int executeRemove(CommandContext<ISender> context) {
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String arg = StringArgumentType.getString(context, "target");
                boolean b = CacheWhitelistDataHandler.removeCacheWhitelist(arg);
                if (b) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(arg)));
                } else {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(arg)));
                }
            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }
}
