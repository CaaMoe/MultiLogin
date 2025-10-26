package moe.caa.multilogin.common.internal.command.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.command.SubCommand;
import moe.caa.multilogin.common.internal.manager.CommandManager;

public class ReloadCommand<S> extends SubCommand<S> {
    public ReloadCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {

    }
}
