package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OnlinePlayerArgumentType implements ArgumentType<Set<IPlayer>> {

    public static OnlinePlayerArgumentType players() {
        return new OnlinePlayerArgumentType();
    }

    public static Set<IPlayer> getPlayers(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Set.class);
    }

    public static IPlayer getPlayer(final CommandContext<?> context, final String name) throws CommandSyntaxException {
        Set<IPlayer> players = getPlayers(context, name);
        if(players.size() == 1){
            return players.iterator().next();
        }
        throw UniversalCommandExceptionType.create(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_multi_target"));
    }

    @SneakyThrows
    @Override
    public Set<IPlayer> parse(StringReader reader) {
        int i = reader.getCursor();
        String string = StringArgumentType.readString(reader);

        UUID uuidOrNull = ValueUtil.getUuidOrNull(string);
        if (uuidOrNull != null) {
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(uuidOrNull);
            if (player == null) {
                reader.setCursor(i);
                throw UniversalCommandExceptionType.create(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_uuid",
                                new Pair<>("uuid", string)
                        ), reader);
            }
            HashSet<IPlayer> players = new HashSet<>();
            players.add(player);
            return players;
        }
        Set<IPlayer> players = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(string);
        if (players.isEmpty()) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_name",
                            new Pair<>("name", string)
                    ), reader);
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