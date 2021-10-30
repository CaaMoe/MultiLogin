package moe.caa.multilogin.core.command.commands.subcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.core.command.commands.BaseRootCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

public abstract class BaseSubCommand extends BaseRootCommand {
    public BaseSubCommand(MultiCore core) {
        super(core);
    }

    public abstract ArgumentBuilder<ISender, ?> getSubExecutor();

    @Override
    public final void register(CommandDispatcher<ISender> dispatcher) {
    }
}
