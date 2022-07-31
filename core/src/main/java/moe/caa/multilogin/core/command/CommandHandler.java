package moe.caa.multilogin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import moe.caa.multilogin.api.command.CommandAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.commands.RootCommand;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandHandler implements CommandAPI {
    @Getter
    private static MultiCore core;
    private final CommandDispatcher<ISender> dispatcher;
    @Getter
    private static BuiltInExceptions builtInExceptions;
    @Getter
    private final SecondaryConfirmationHandler secondaryConfirmationHandler;

    public CommandHandler(MultiCore core) {
        CommandHandler.core = core;
        this.dispatcher = new CommandDispatcher<>();
        this.secondaryConfirmationHandler = new SecondaryConfirmationHandler();
    }

    public void init() {
        dispatcher.register(new RootCommand(this).register(literal("multilogin")));
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = CommandHandler.builtInExceptions =
                new BuiltInExceptions(core);
    }

    @Override
    public void execute(ISender sender, String[] args) {
        core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                dispatcher.execute(String.join(" ", args), sender);
            } catch (CommandSyntaxException e) {
                sender.sendMessagePL(e.getRawMessage().getString());
            } catch (Exception e) {
                sender.sendMessagePL(core.getLanguageHandler().getMessage("command_error"));
                LoggerProvider.getLogger().error(String.format("An exception occurs when the %s command is executed.", String.join(" ", args)), e);
            }
        });
    }

    @Override
    public List<String> tabComplete(ISender sender, String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_TAB_COMPLETE)) {
            return Collections.emptyList();
        }
        CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(String.join(" ", args), sender));
        List<String> ret = new ArrayList<>();
        try {
            Suggestions suggestions1 = suggestions.get();
            for (Suggestion suggestion : suggestions1.getList()) {
                ret.add(suggestion.getText());
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().error(String.format("An exception occurred while executing the %s command to complete.", String.join(" ", args)), e);
        }
        return ret;
    }

    public final LiteralArgumentBuilder<ISender> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public final <T> RequiredArgumentBuilder<ISender, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
