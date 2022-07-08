package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * 修复的中文指令参数阅读程序
 */
public class StringArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList(
            "string",
            "哼哼啊啊啊啊啊"
    );

    public static StringArgumentType string() {
        return new StringArgumentType();
    }

    public static String getString(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        return reader.getString().substring(argBeginning, reader.getCursor());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}