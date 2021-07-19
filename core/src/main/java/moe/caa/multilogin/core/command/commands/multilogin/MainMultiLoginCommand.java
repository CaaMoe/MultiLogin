package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.command.commands.multilogin.query.MainQueryCommand;

public class MainMultiLoginCommand extends SubCommand {
    public MainMultiLoginCommand() {
        super("multilogin", null);
        subCommands.add(new ReloadMultiLoginCommand());
        subCommands.add(new MainQueryCommand());
    }
}
