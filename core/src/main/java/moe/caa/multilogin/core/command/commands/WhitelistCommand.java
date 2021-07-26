package moe.caa.multilogin.core.command.commands;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Collections;
import java.util.List;

public class WhitelistCommand extends SubCommand {

    public WhitelistCommand(MultiCore core) {
        super(core, null);
    }

    public SubCommand registerSub() {
        subCommandMap.put("add", new SubCommand(core, Permission.MULTI_LOGIN_WHITELIST) {
            @Override
            protected boolean execute(ISender sender, String[] args) {
                if (args.length != 1) return false;
                executeAdd(sender, args[0]);
                return true;
            }

            @Override
            protected List<String> tabCompete(ISender sender, String[] args) {
                return Collections.emptyList();
            }
        });
        subCommandMap.put("remove", new SubCommand(core, Permission.MULTI_LOGIN_WHITELIST) {
            @Override
            protected boolean execute(ISender sender, String[] args) {
                if (args.length != 1) return false;
                executeRemove(sender, args[0]);
                return true;
            }

            @Override
            protected List<String> tabCompete(ISender sender, String[] args) {
                return Collections.emptyList();
            }
        });
        return this;
    }

    private void executeAdd(ISender sender, String name) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                boolean b = core.getSqlManager().getCacheWhitelistDataHandler().addCacheWhitelist(name);
                if (b) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD.getMessage(core, name)));
                } else {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_ADD_ALREADY.getMessage(core, name)));
                }
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
    }

    private int executeRemove(ISender sender, String name) {
        core.plugin.getSchedule().runTaskAsync(() -> {
            try {
                boolean b = core.getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(name);
                if (b) {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(core, name)));
                } else {
                    core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(core, name)));
                }
            } catch (Throwable throwable) {
                core.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_ERROR.getMessage(core)));
                core.getLogger().log(LoggerLevel.ERROR, throwable);
                core.getLogger().log(LoggerLevel.ERROR, LanguageKeys.COMMAND_ERROR.getMessage(core));
            }
        });
        return 0;
    }
}
