package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.There;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.database.table.UserDataTableV3;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Online 参数阅读程序
 * <service_id> <online_uuid|online_name>
 */
public class OnlineArgumentType implements ArgumentType<OnlineArgumentType.OnlineArgument> {

    public static OnlineArgumentType online() {
        return new OnlineArgumentType();
    }

    public static OnlineArgumentType.OnlineArgument getOnline(final CommandContext<?> context, final String name) {
        return context.getArgument(name, OnlineArgumentType.OnlineArgument.class);
    }

    @SneakyThrows
    @Override
    public OnlineArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.readServiceConfig(reader);
        if (!reader.canRead()) {
            reader.setCursor(i);
            throw CommandHandler.getBuiltInExceptions().dispatcherUnknownCommand().createWithContext(reader);
        }
        reader.skip();
        String nameOrUuid = StringArgumentType.readString(reader);

        UserDataTableV3 dataTable = CommandHandler.getCore().getSqlManager().getUserDataTable();

        UUID uuid = ValueUtil.getUuidOrNull(nameOrUuid);
        if (uuid == null) {
            uuid = dataTable.getOnlineUUID(nameOrUuid, serviceConfig.getId());
            if (uuid == null) {
                reader.setCursor(i);
                throw UniversalCommandExceptionType.create(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_online_not_found_by_name",
                                new Pair<>("service_name", serviceConfig.getName()),
                                new Pair<>("service_id", serviceConfig.getId()),
                                new Pair<>("online_name", nameOrUuid)
                        ), reader);
            }
        }
        There<String, UUID, Boolean> there = dataTable.get(uuid, serviceConfig.getId());
        if (there == null) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_online_not_found_by_uuid",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", uuid)
                    ), reader);
        }
        return new OnlineArgument(serviceConfig, uuid, there.getValue1(), there.getValue2(), there.getValue3());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ServiceIdArgumentType.getSuggestions(context, builder);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class OnlineArgument {
        private final BaseServiceConfig baseServiceConfig;
        private final UUID onlineUUID;
        private final String onlineName;
        // Nullable
        private final UUID profileUUID;
        private final boolean whitelist;
    }
}
