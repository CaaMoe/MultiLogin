package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;

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
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String uuidString = reader.getString().substring(argBeginning, reader.getCursor());
        UUID ret = ValueUtil.getUuidOrNull(uuidString);
        if (ret == null) {
            throw CommandHandler.getBuiltInExceptions().readerInvalidUUID().createWithContext(reader, uuidString);
        }
        return ret;
    }
}