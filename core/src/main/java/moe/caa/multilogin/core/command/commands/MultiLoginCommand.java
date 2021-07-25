package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.List;
import java.util.UUID;

public class MultiLoginCommand {
    private final MultiCore core;

    public MultiLoginCommand(MultiCore core) {
        this.core = core;
    }

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                //根命令
                CommandHandler.literal("multilogin")
                        //二级命令
                        .then(CommandHandler.literal("query")
                                        //三级命令
                                        .then(CommandHandler.literal("name")
                                                //需求字符串参数
                                                .then(CommandHandler.argument("target", StringArgumentType.greedyString())
                                                        //执行
                                                        .executes(this::executeName)
                                                )
                                        ).then(CommandHandler.literal("onlineuuid")
                                                .then(CommandHandler.argument("target", StringArgumentType.greedyString())
                                                        .executes(this::executeOnlineUuid)
                                                )
                                        ).then(CommandHandler.literal("redirectuuid")
                                                .then(CommandHandler.argument("target", StringArgumentType.greedyString())
                                                        .executes(this::executeRedirectUuid)
                                                )
                                        )
                                //权限
                        ).requires(Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY::hasPermission)
                        .then(CommandHandler.literal("reload").requires(Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD::hasPermission)
                                .executes(this::executeReload)
                        )

        );

    }

    private int executeReload(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                core.reload();
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_RELOADED.getMessage(core)));
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }

    private int executeRedirectUuid(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String uuidString = StringArgumentType.getString(context, "target");
                UUID uuid = ValueUtil.getUUIDOrNull(uuidString);
                if (uuid == null) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(core, uuidString)));
                    return;
                }
                List<User> users = core.getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(uuid);
                if (users.size() == 0) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_REDIRECT_UUID.getMessage(core, uuidString)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(core, users.size()));
                    for (User user : users) {
                        context.getSource().sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }

    private int executeOnlineUuid(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String uuidString = StringArgumentType.getString(context, "target");
                UUID uuid = ValueUtil.getUUIDOrNull(uuidString);
                if (uuid == null) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(core, uuidString)));
                    return;
                }
                User user = core.getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(uuid);
                if (user == null) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_ONLINE_UUID.getMessage(core, uuidString)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(toMessage(user));
                });
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }

    private int executeName(CommandContext<ISender> context) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                String s = StringArgumentType.getString(context, "target");
                List<User> users = core.getSqlManager().getUserDataHandler().getUserEntryByCurrentName(s);
                if (users.size() == 0) {
                    core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_UNKNOWN_NAME.getMessage(core, s)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    context.getSource().sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(core, users.size()));
                    for (User user : users) {
                        context.getSource().sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> context.getSource().sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }

    protected String toMessage(User user) {
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(core, user.getCurrentName(), user.getOnlineUuid().toString(), user.getRedirectUuid().toString(), user.getService().getName(), user.getService().getPath(), user.isWhitelist());
    }
}
