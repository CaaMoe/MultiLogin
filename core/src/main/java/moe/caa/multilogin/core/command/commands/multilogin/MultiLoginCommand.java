package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.commands.multilogin.independent.ReloadCommand;
import moe.caa.multilogin.core.command.commands.multilogin.query.QueryCommand;
import moe.caa.multilogin.core.command.impl.FatherCommand;

public class MultiLoginCommand extends FatherCommand {
    public MultiLoginCommand() {
        super(null);
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("query", new QueryCommand());
    }
}
