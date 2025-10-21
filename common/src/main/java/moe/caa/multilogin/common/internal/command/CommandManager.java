package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import moe.caa.multilogin.common.internal.command.sub.HelpCommand;
import moe.caa.multilogin.common.internal.command.sub.InfoCommand;
import moe.caa.multilogin.common.internal.command.sub.SubCommand;
import moe.caa.multilogin.common.internal.main.MultiCore;

import java.util.List;

public abstract class CommandManager<S> {
    public final MultiCore core;
    public final List<SubCommand<S>> subCommands;

    public final HelpCommand<S> helpCommand;

    public CommandManager(MultiCore core) {
        this.core = core;

        this.subCommands = List.of(
                helpCommand = new HelpCommand<>(this),
                new InfoCommand<>(this)
        );
    }

    public LiteralArgumentBuilder<S> commandBuilder() {
        LiteralArgumentBuilder<S> literal = LiteralArgumentBuilder.literal("multilogin");
        for (SubCommand<S> command : subCommands) {
            command.register(literal);
        }
        literal.executes(context -> {
            executeAsync(() -> helpCommand.showHelp(context.getSource()));
            return Command.SINGLE_SUCCESS;
        });
        return literal;
    }

    public LiteralCommandNode<S> buildCommand() {
        LiteralArgumentBuilder<S> literal = commandBuilder();
        return literal.build();
    }

    public void executeAsync(Runnable runnable) {
        core.asyncExecutor.execute(runnable);
    }

    public abstract Sender wrapSender(S s);
}
