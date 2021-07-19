package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

public class DelWhitelistCommand extends SubCommand {
    protected DelWhitelistCommand() {
        super("remove", Permission.MULTI_LOGIN_WHITELIST_REMOVE);
    }

    @Override
    public void executeAsync(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            boolean result = CacheWhitelistDataHandler.removeCacheWhitelist(args[0]);
            if (result) {
                MultiCore.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(args[0])));
            } else {
                MultiCore.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(args[0])));
            }
        } else {
            sendUnknownCommandMessage(sender);
        }
    }
}
