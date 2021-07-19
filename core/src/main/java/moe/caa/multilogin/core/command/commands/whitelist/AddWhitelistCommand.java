package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.impl.ISender;

import java.util.Arrays;

public class AddWhitelistCommand extends SubCommand {

    protected AddWhitelistCommand() {
        super("add", Permission.MULTI_LOGIN_WHITELIST_ADD);
    }

    @Override
    public void execute(ISender sender, String[] args) {
        sender.sendMessage(Arrays.toString(args));
    }
}
