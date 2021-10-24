package moe.caa.multilogin.core.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YggdrasilServiceArgumentType implements ArgumentType<YggdrasilService> {
    private static final Collection<String> EXAMPLES = Arrays.asList(
            "official",
            "demoYggdrasilPath"
    );

    private static final DynamicCommandExceptionType dynamicCommandExceptionType = new DynamicCommandExceptionType(path -> new LiteralMessage(CommandHandler.getCore().getLanguageHandler().getMessage("command_exception_reader_invalid_yggdrasil", FormatContent.createContent(
            FormatContent.FormatEntry.builder().name("path").content(path).build()
    ))));

    public static YggdrasilServiceArgumentType yggdrasil() {
        return new YggdrasilServiceArgumentType();
    }

    public static YggdrasilService getYggdrasil(final CommandContext<?> context, final String name) {
        return context.getArgument(name, YggdrasilService.class);
    }

    @Override
    public YggdrasilService parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String path = reader.getString().substring(argBeginning, reader.getCursor());
        YggdrasilService yggdrasilService = CommandHandler.getCore().getYggdrasilServicesHandler().getYggdrasilService(path);
        if (yggdrasilService == null) {
            throw dynamicCommandExceptionType.create(path);
        }
        return yggdrasilService;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        for (YggdrasilService service : CommandHandler.getCore().getYggdrasilServicesHandler().getAllServices()) {
            if (service.getPath().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                builder.suggest(service.getPath());
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
