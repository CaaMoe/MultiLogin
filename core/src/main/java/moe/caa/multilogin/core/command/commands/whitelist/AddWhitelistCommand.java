package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.impl.ISender;

public class AddWhitelistCommand extends SubCommand {

    protected AddWhitelistCommand() {
        super("add", Permission.MULTI_LOGIN_WHITELIST_ADD);
    }

    @Override
    public void execute(ISender sender, String[] args) throws Throwable {
        if(args.length == 1){
            boolean result = CacheWhitelistDataHandler.addCacheWhitelist(args[0]);

        } else {
            super.execute(sender, args);
        }

    }
}
