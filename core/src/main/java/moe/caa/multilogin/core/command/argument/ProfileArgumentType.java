package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.Data;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;
import moe.caa.multilogin.core.database.table.InGameProfileTableV3;

import java.util.UUID;

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
                                new Pair<>("profile_name", nameOrUuid)
                        ), reader);
            }

            return new ProfileArgument(uuid, table.getUsername(uuid));
        }
        String username = table.getUsername(uuid);
        if (username == null) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_not_found_by_uuid",
                            new Pair<>("profile_uuid", uuid)
                    ), reader);
        }
        return new ProfileArgument(uuid, username);
    }

    @Data
    public static class ProfileArgument {
        private final UUID profileUUID;
        private final String profileName;


    }
}
