package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import moe.caa.multilogin.common.internal.command.sub.HelpCommand;
import moe.caa.multilogin.common.internal.command.sub.SubCommand;
import moe.caa.multilogin.common.internal.main.MultiCore;

import java.util.List;

public class CommandManager<SENDER> {
    public final MultiCore core;
    public final SenderUnwrapper<SENDER> senderUnwrapper;
    public final List<SubCommand<SENDER>> subCommands;

    public final HelpCommand<SENDER> helpCommand = new HelpCommand<>(this);

    public CommandManager(MultiCore core, SenderUnwrapper<SENDER> senderUnwrapper) {
        this.core = core;
        this.senderUnwrapper = senderUnwrapper;

        this.subCommands = List.of(
                helpCommand
        );
    }

    public LiteralArgumentBuilder<SENDER> commandBuilder() {
        LiteralArgumentBuilder<SENDER> literal = LiteralArgumentBuilder.literal("multilogin");
        for (SubCommand<SENDER> command : subCommands) {
            command.register(literal);
        }
        literal.executes(context -> {
            helpCommand.showHelp(context.getSource());
            return Command.SINGLE_SUCCESS;
        });
        return literal;
    }

    public LiteralCommandNode<SENDER> buildCommand() {
        LiteralArgumentBuilder<SENDER> literal = commandBuilder();
        return literal.build();
    }
}
