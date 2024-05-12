package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Data;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;
import moe.caa.multilogin.core.database.table.InGameProfileTableV3;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Profile 参数阅读程序
 * <profile_name|profile_uuid>
 */
public class ProfileArgumentType implements ArgumentType<ProfileArgumentType.ProfileArgument> {

    public static ProfileArgumentType profile() {
        return new ProfileArgumentType();
    }

    public static ProfileArgument getProfile(final CommandContext<?> context, final String name) {
        return context.getArgument(name, ProfileArgument.class);
    }

    @SneakyThrows
    @Override
    public ProfileArgument parse(StringReader reader) {
        int i = reader.getCursor();

        String nameOrUuid = StringArgumentType.readString(reader);

        InGameProfileTableV3 table = CommandHandler.getCore().getSqlManager().getInGameProfileTable();

        UUID uuid = ValueUtil.getUuidOrNull(nameOrUuid);
        if (uuid == null) {
            uuid = table.getInGameUUIDIgnoreCase(nameOrUuid);
            if (uuid == null) {
                reader.setCursor(i);
                throw UniversalCommandExceptionType.create(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_not_found_by_name",
                                new Pair<>("name", nameOrUuid)
                        ), reader);
            }

            return new ProfileArgument(uuid, table.getUsername(uuid));
        }
        String username = table.getUsername(uuid);
        if (username == null) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_not_found_by_uuid",
                            new Pair<>("uuid", uuid)
                    ), reader);
        }
        return new ProfileArgument(uuid, username);
    }

    @Data
    public static class ProfileArgument {
        private final UUID profileUUID;
        // Nullable!!!
        private final String profileName;
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
