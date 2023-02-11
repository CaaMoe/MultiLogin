package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

/**
 * Service 参数阅读程序
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceIdArgumentType implements ArgumentType<BaseServiceConfig> {

    public static ServiceIdArgumentType service() {
        return new ServiceIdArgumentType();
    }

    public static BaseServiceConfig getService(final CommandContext<?> context, final String name) {
        return context.getArgument(name, BaseServiceConfig.class);
    }

    @Override
    public BaseServiceConfig parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final int result = reader.readInt();
        BaseServiceConfig config = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(result);
        if (config == null) {
            throw CommandHandler.getBuiltInExceptions().serviceidNotFound().createWithContext(reader, result);
        }
        return config;
    }
}
