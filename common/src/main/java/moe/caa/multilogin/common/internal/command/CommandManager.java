package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import moe.caa.multilogin.common.internal.command.sub.HelpCommand;
import moe.caa.multilogin.common.internal.command.sub.SubCommand;
import moe.caa.multilogin.common.internal.main.MultiCore;

import java.util.List;
import java.util.function.Function;

public class CommandManager<SENDER> {
    public final MultiCore core;
    public final Function<SENDER, CMDSender> senderMap;
    public final List<SubCommand<SENDER>> subCommands;

    public CommandManager(MultiCore core, Function<SENDER, CMDSender> senderMap) {
        this.core = core;
        this.senderMap = senderMap;

        this.subCommands = List.of(
                new HelpCommand<>(this)
        );
    }

    public LiteralArgumentBuilder<SENDER> commandBuilder() {
        LiteralArgumentBuilder<SENDER> literal = LiteralArgumentBuilder.literal("multilogin");
        for (SubCommand<SENDER> command : subCommands) {
            command.register(literal);
        }
        return literal;
    }

    public LiteralCommandNode<SENDER> buildCommand() {
        LiteralArgumentBuilder<SENDER> literal = commandBuilder();
        return literal.build();
    }
}
