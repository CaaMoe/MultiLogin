package moe.caa.multilogin.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import moe.caa.multilogin.fabric.impl.FabricSender;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MultiLoginCommand {
    private final MultiLoginFabric plugin;

    public MultiLoginCommand(MultiLoginFabric plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("multilogin")
                        .then(CommandManager.argument("args", StringArgumentType.greedyString())
                                .suggests(this::suggests)
                                .executes(this::execute)
                        )
        );
    }

    private CompletableFuture<Suggestions> suggests(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        FabricSender sender = new FabricSender(context.getSource());
        String s = builder.getRemainingLowerCase();
        String[] args = s.split("\\s+");
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = "multilogin";
        // todo 有问题的
        List<String> list = plugin.getApi().getCommandHandler().tabComplete(sender, ns);
        list.forEach(builder::suggest);
        return builder.buildFuture();
    }

    private int execute(CommandContext<ServerCommandSource> context) {
        FabricSender sender = new FabricSender(context.getSource());
        String[] args = StringArgumentType.getString(context, "args").split("\\s+");
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = "multilogin";
        plugin.getApi().getCommandHandler().execute(sender, ns);
        return 0;
    }
}
