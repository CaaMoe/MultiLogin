package moe.caa.multilogin.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import moe.caa.multilogin.core.command.argument.suggestion.Suggestion;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 修复的中文指令参数阅读程序
 */
public class StringArgumentType implements ArgumentType<String> {

    private final Suggestion suggestion;

    public StringArgumentType(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public static StringArgumentType string() {
        return new StringArgumentType((s) -> new HashSet<>());
    }

    public static StringArgumentType string(Suggestion suggestion){
        return new StringArgumentType(suggestion);
    }

    public static String getString(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    public static String readString(StringReader reader) {
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
    public String parse(StringReader reader) {
        return readString(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String s : suggestion.suggestion(builder.getRemaining())) {
            builder.suggest(s);
        }
        return builder.buildFuture();
    }
}