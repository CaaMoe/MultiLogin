package moe.caa.multilogin.fabric.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import moe.caa.multilogin.fabric.impl.FabricSender;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MultiLoginCommand implements SuggestionProvider<ServerCommandSource>, Command<ServerCommandSource> {
    private final MultiLoginFabric plugin;

    public MultiLoginCommand(MultiLoginFabric plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("multilogin")
                        .executes(this)
                        .then(CommandManager.argument("args", StringArgumentType.greedyString())
                                .suggests(this)
                                .executes(this)
                        )
        );
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        FabricSender sender = new FabricSender(context.getSource());
        String input = builder.getInput();
        builder = builder.createOffset(input.lastIndexOf(' ') + 1);
        List<String> list = plugin.getApi().getCommandHandler().tabComplete(sender, input.substring(1));
        list.forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        String input = context.getRange().get(context.getInput());
        FabricSender sender = new FabricSender(context.getSource());
        plugin.getApi().getCommandHandler().execute(sender, input);
        return 0;
    }
}
