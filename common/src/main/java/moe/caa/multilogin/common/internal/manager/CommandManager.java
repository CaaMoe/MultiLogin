package moe.caa.multilogin.common.internal.manager;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import moe.caa.multilogin.common.internal.command.*;
import moe.caa.multilogin.common.internal.command.admin.ReloadCommand;
import moe.caa.multilogin.common.internal.data.Sender;
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
                new InfoCommand<>(this),
                new CreateCommand<>(this),
                new ProfileCommand<>(this),
                new ReloadCommand<>(this)
        );
    }

    public LiteralArgumentBuilder<S> commandBuilder() {
        LiteralArgumentBuilder<S> literal = LiteralArgumentBuilder.literal("multilogin");
        for (SubCommand<S> command : subCommands) {
            command.register(literal);
        }
        literal.executes(context -> {
            return executeAsync(context, () -> helpCommand.showHelp(context.getSource()));
        });
        return literal;
    }

    public LiteralCommandNode<S> buildCommand() {
        LiteralArgumentBuilder<S> literal = commandBuilder();
        return literal.build();
    }

    public int executeAsync(CommandContext<S> context, ThrowRunnable runnable) {
        core.virtualPerTaskExecutor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                core.platform.getPlatformLogger().error("Failed to execute command:" + context.getInput(), e);
                wrapSender(context.getSource()).sendMessage(core.messageConfig.commandGeneralError.get().build());
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    public abstract Sender wrapSender(S s);

    @FunctionalInterface
    public interface ThrowRunnable {
        void run() throws Throwable;
    }
}
