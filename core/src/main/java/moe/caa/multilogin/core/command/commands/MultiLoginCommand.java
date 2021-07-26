package moe.caa.multilogin.core.command.commands;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.command.commands.query.QueryCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Collections;
import java.util.List;

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

        subCommandMap.put("query", new QueryCommand(core).registerSub());
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
}
