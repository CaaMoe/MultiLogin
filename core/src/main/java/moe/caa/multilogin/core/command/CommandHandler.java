package moe.caa.multilogin.core.command;

import moe.caa.multilogin.api.command.CommandAPI;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.List;

public class CommandHandler implements CommandAPI {
    private final MultiCore core;

    public CommandHandler(MultiCore core) {
        this.core = core;
    }

    @Override
    public void execute(ISender sender, String[] args) {

    }

    @Override
    public List<String> tabComplete(ISender sender, String[] args) {
        return List.of();
    }
}
