package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.SubCommand;

public class MainWhitelistCommand extends SubCommand {
    public MainWhitelistCommand() {
        super("whitelist", null);
        subCommands.add(new AddWhitelistCommand());
        subCommands.add(new DelWhitelistCommand());
    }
}
