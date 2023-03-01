package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OnlinePlayerArgumentType implements ArgumentType<Set<IPlayer>> {

    public static OnlinePlayerArgumentType player() {
        return new OnlinePlayerArgumentType();
    }

    public static Set<IPlayer> getPlayer(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Set.class);
    }

    @SneakyThrows
    @Override
    public Set<IPlayer> parse(StringReader reader) throws CommandSyntaxException {
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
        if (players.size() == 0) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_name",
                            new Pair<>("name", string)
                    ), reader);
        }
        return players;
    }

}