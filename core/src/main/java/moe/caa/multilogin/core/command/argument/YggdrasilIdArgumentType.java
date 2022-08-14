package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

/**
 * Yggdrasil 参数阅读程序
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YggdrasilIdArgumentType implements ArgumentType<YggdrasilServiceConfig> {

    public static YggdrasilIdArgumentType yggdrasilid() {
        return new YggdrasilIdArgumentType();
    }

    public static YggdrasilServiceConfig getYggdrasil(final CommandContext<?> context, final String name) {
        return context.getArgument(name, YggdrasilServiceConfig.class);
    }

    @Override
    public YggdrasilServiceConfig parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final int result = reader.readInt();
        YggdrasilServiceConfig config = CommandHandler.getCore().getPluginConfig().getIdMap().get(result);
        if (config == null) {
            throw CommandHandler.getBuiltInExceptions().yggdrasilidNotFound().createWithContext(reader, result);
        }
        return config;
    }
}
