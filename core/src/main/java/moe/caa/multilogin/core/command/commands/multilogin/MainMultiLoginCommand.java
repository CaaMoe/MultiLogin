package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.SubCommand;

public class MainMultiLoginCommand extends SubCommand {
    public MainMultiLoginCommand() {
        super("multilogin", null);
        subCommands.add(new ReloadMultiLoginCommand());
    }
}
