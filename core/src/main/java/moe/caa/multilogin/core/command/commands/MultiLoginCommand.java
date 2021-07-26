package moe.caa.multilogin.core.command.commands;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MultiLoginCommand extends SubCommand {

    public MultiLoginCommand(MultiCore core) {
        super(core, null);
    }

    public SubCommand registerSub() {
        subCommandMap.put("reload", new SubCommand(core, Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD) {
            @Override
            protected boolean execute(ISender sender, String[] args) {
                if (args.length != 0) return false;
                executeReload(sender);
                return true;
            }

            @Override
            protected List<String> tabCompete(ISender sender, String[] args) {
                return Collections.emptyList();
            }
        });

        subCommandMap.put("query", new SubCommand(core, Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY) {
            public SubCommand registerSub() {
                subCommandMap.put("name", new SubCommand(core, Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY) {
                    @Override
                    protected boolean execute(ISender sender, String[] args) {
                        if (args.length != 1) return false;
                        executeName(sender, args[0]);
                        return true;
                    }

                    @Override
                    protected List<String> tabCompete(ISender sender, String[] args) {
                        return Collections.emptyList();
                    }
                });

                subCommandMap.put("redirectname", new SubCommand(core, Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY) {
                    @Override
                    protected boolean execute(ISender sender, String[] args) {
                        if (args.length != 1) return false;
                        executeRedirectUuid(sender, args[0]);
                        return true;
                    }

                    @Override
                    protected List<String> tabCompete(ISender sender, String[] args) {
                        return Collections.emptyList();
                    }
                });

                subCommandMap.put("onlinename", new SubCommand(core, Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY) {
                    @Override
                    protected boolean execute(ISender sender, String[] args) {
                        if (args.length != 1) return false;
                        executeOnlineUuid(sender, args[0]);
                        return true;
                    }

                    @Override
                    protected List<String> tabCompete(ISender sender, String[] args) {
                        return Collections.emptyList();
                    }
                });
                return this;
            }
        }.registerSub());
        return this;
    }

    private void executeReload(ISender sender) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                core.reload();
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_RELOADED.getMessage(core)));
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
    }

    private void executeRedirectUuid(ISender sender, String redirectUuid) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                UUID uuid = ValueUtil.getUUIDOrNull(redirectUuid);
                if (uuid == null) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(core, redirectUuid)));
                    return;
                }
                List<User> users = core.getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(uuid);
                if (users.size() == 0) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_REDIRECT_UUID.getMessage(core, redirectUuid)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    sender.sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(core, users.size()));
                    for (User user : users) {
                        sender.sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
    }

    private void executeOnlineUuid(ISender sender, String onlineUuid) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                UUID uuid = ValueUtil.getUUIDOrNull(onlineUuid);
                if (uuid == null) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(core, onlineUuid)));
                    return;
                }
                User user = core.getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(uuid);
                if (user == null) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_ONLINE_UUID.getMessage(core, onlineUuid)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    sender.sendMessage(toMessage(user));
                });
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
    }

    private void executeName(ISender sender, String name) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                List<User> users = core.getSqlManager().getUserDataHandler().getUserEntryByCurrentName(name);
                if (users.size() == 0) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_NAME.getMessage(core, name)));
                    return;
                }
                core.plugin.getSchedule().runTask(() -> {
                    sender.sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(core, users.size()));
                    for (User user : users) {
                        sender.sendMessage(toMessage(user));
                    }
                });

            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
    }

    protected String toMessage(User user) {
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(core, user.getCurrentName(), user.getOnlineUuid().toString(), user.getRedirectUuid().toString(), user.getService().getName(), user.getService().getPath(), user.isWhitelist());
    }
}
