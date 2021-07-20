package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.data.database.handler.UserDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.List;
import java.util.UUID;

public class MultiLoginCommand {

    public static void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                CommandHandler.literal("multilogin")
                        .then(CommandHandler.literal("query")
                                .then(CommandHandler.literal("name")
                                        .then(CommandHandler.argument("target", StringArgumentType.string())
                                                .executes(MultiLoginCommand::executeName)
                                        )
                                ).then(CommandHandler.literal("onlineuuid")
                                        .then(CommandHandler.argument("target", StringArgumentType.string())
                                                .executes(MultiLoginCommand::executeOnlineUuid)
                                        )
                                ).then(CommandHandler.literal("redirectuuid")
                                        .then(CommandHandler.argument("target", StringArgumentType.string())
                                                .executes(MultiLoginCommand::executeRedirectUuid)
                                        )
                                )
                        ).requires(Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY::hasPermission)
                        .then(CommandHandler.literal("reload").requires(Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD::hasPermission)
                                .executes(MultiLoginCommand::executeReload)
                        )

        );

    }

    private static int executeReload(CommandContext<ISender> context) {
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                MultiCore.reload();
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_RELOADED.getMessage()));
            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }

    private static int executeRedirectUuid(CommandContext<ISender> context) {
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String uuidString = StringArgumentType.getString(context, "target");
                UUID uuid = ValueUtil.getUUIDOrNull(uuidString);
                if (uuid == null) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(uuidString)));
                    return;
                }
                List<User> users = UserDataHandler.getUserEntryByRedirectUuid(uuid);
                if (users.size() == 0) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_REDIRECT_UUID.getMessage(uuidString)));
                    return;
                }
                MultiCore.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(users.size()));
                    for (User user : users) {
                        context.getSource().sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }

    private static int executeOnlineUuid(CommandContext<ISender> context) {
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String uuidString = StringArgumentType.getString(context, "target");
                UUID uuid = ValueUtil.getUUIDOrNull(uuidString);
                if (uuid == null) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(uuidString)));
                    return;
                }
                User user = UserDataHandler.getUserEntryByOnlineUuid(uuid);
                if (user == null) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_ONLINE_UUID.getMessage(uuidString)));
                    return;
                }
                MultiCore.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(toMessage(user));
                });
            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }

    private static int executeName(CommandContext<ISender> context) {
        MultiCore.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String s = StringArgumentType.getString(context, "target");
                List<User> users = UserDataHandler.getUserEntryByCurrentName(s);
                if (users.size() == 0) {
                    MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_NAME.getMessage(s)));
                    return;
                }
                MultiCore.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(users.size()));
                    for (User user : users) {
                        context.getSource().sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                MultiCore.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage()));
                MultiLogger.log(LoggerLevel.ERROR, throwable);
                MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage());
            }
        });
        return 0;
    }

    protected static String toMessage(User user) {
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(user.currentName, user.onlineUuid.toString(), user.redirectUuid.toString(), user.service.getName(), user.service.getPath(), user.whitelist);
    }
}
