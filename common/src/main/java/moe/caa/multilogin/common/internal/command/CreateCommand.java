package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.manager.CommandManager;

public class CreateCommand<S> extends SubCommand<S> {
    public CreateCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionCreate = "multilogin.command.create";
    }
}

