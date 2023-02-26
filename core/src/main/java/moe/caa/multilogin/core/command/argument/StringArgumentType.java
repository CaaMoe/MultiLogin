package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * 修复的中文指令参数阅读程序
 */
public class StringArgumentType implements ArgumentType<String> {

    public static StringArgumentType string() {
        return new StringArgumentType();
    }

    public static String getString(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    public static String readString(StringReader reader) {
        int argBeginning = reader.getCursor();
        // 如果能读，并且下一个格子内容不是空
        while (reader.canRead() && reader.peek() != ' ') {
            // 游标++
            reader.skip();
        }
        return reader.getString().substring(argBeginning, reader.getCursor());
    }

    @Override
    public String parse(StringReader reader) {
        return readString(reader);
    }
}