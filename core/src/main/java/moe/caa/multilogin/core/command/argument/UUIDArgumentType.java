package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;

import java.util.UUID;

/**
 * UUID 参数阅读程序
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UUIDArgumentType implements ArgumentType<UUID> {

    public static UUIDArgumentType uuid() {
        return new UUIDArgumentType();
    }

    public static UUID getUuid(final CommandContext<?> context, final String name) {
        return context.getArgument(name, UUID.class);
    }

    @Override
    public UUID parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();

        String uuidString = StringArgumentType.readString(reader);
        UUID ret = ValueUtil.getUuidOrNull(uuidString);
        if (ret == null) {
            reader.setCursor(argBeginning);
            throw UniversalCommandExceptionType.create(CommandHandler.getCore().getLanguageHandler().getMessage("command_exception_reader_invalid_uuid",
                    new Pair<>("value", uuidString)
            ), reader);
        }
        return ret;
    }
}
