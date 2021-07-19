package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.impl.FatherCommand;

//whitelist父节点
public class WhitelistCommand extends FatherCommand {
    public WhitelistCommand() {
        super(null);
        subCommands.put("add", new Whitelist_AddCommand());
        subCommands.put("remove", new Whitelist_RemoveCommand());
    }
}
