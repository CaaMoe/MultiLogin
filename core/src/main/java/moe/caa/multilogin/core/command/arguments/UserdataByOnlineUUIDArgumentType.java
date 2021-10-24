package moe.caa.multilogin.core.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserdataByOnlineUUIDArgumentType implements ArgumentType<User> {

    private static final DynamicCommandExceptionType dynamicCommandExceptionType = new DynamicCommandExceptionType(value -> new LiteralMessage(CommandHandler.getCore().getLanguageHandler().getMessage("command_exception_reader_invalid_user", FormatContent.createContent(
            FormatContent.FormatEntry.builder().name("online_uuid").content(value).build()
    ))));

    public static UserdataByOnlineUUIDArgumentType user() {
        return new UserdataByOnlineUUIDArgumentType();
    }

    public static User getUser(final CommandContext<?> context, final String name) {
        return context.getArgument(name, User.class);
    }

    @SneakyThrows
    @Override
    public User parse(StringReader reader) {
        UUID parse = UUIDArgumentType.uuid().parse(reader);
        User userEntryByOnlineUuid = CommandHandler.getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(parse);
        if(userEntryByOnlineUuid == null){
            throw dynamicCommandExceptionType.create(parse.toString());
        }
        return userEntryByOnlineUuid;
    }

    @Override
    public Collection<String> getExamples() {
        return UUIDArgumentType.EXAMPLES;
    }
}
