package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.core.command.CommandHandler;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 表示一个在线玩家的参数
 */
public class OnlinePlayersArgumentType implements ArgumentType<Set<IPlayer>> {

    public static OnlinePlayersArgumentType players() {
        return new OnlinePlayersArgumentType();
    }

    public static Set<IPlayer> getPlayers(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Set.class);
    }

    @Override
    public Set<IPlayer> parse(StringReader reader) throws CommandSyntaxException {
        String result = StringArgumentType.readString(reader);
        Set<IPlayer> players = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(result);

        if (players.size() == 0) {
            throw CommandHandler.getBuiltInExceptions().playerNotOnline().create(result);
        }
        return players;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (IPlayer key : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers()) {
            if (key.getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                builder.suggest(key.getName());
        }
        return builder.buildFuture();
    }
}
