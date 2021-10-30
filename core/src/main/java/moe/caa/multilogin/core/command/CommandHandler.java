package moe.caa.multilogin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import moe.caa.multilogin.core.command.commands.RootMultiLoginCommand;
import moe.caa.multilogin.core.command.commands.RootWhitelistCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 命令处理程序
 */
public class CommandHandler {
    @Getter()
    private static MultiCore core;
    private final CommandDispatcher<ISender> dispatcher = new CommandDispatcher<>();

    public CommandHandler(MultiCore core) {
        CommandHandler.core = core;
    }

    public void init() {
        new RootMultiLoginCommand(core).register(dispatcher);
        new RootWhitelistCommand(core).register(dispatcher);
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new BuiltInExceptions(core);
    }

    public void execute(ISender sender, String[] args) {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Executing command: %s. (%s)", String.join(" ", args), sender.getName()));
        core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                dispatcher.execute(String.join(" ", args), sender);
            } catch (CommandSyntaxException e) {
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, "arguments: " + String.join(" ", args), e);
                sender.sendMessage(e.getRawMessage().getString());
            } catch (Exception e) {
                sender.sendMessage(core.getLanguageHandler().getMessage("command_error", FormatContent.empty()));
                MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while executing the command.", e);
                MultiLogger.getLogger().log(LoggerLevel.ERROR, "sender: " + sender.getName());
                MultiLogger.getLogger().log(LoggerLevel.ERROR, "arguments: " + String.join(" ", args));
            }
        });

    }

    public List<String> tabComplete(ISender sender, String[] ns) {
        if(!sender.hasPermission(Permissions.COMMAND_TAB_COMPLETE)){
            return Collections.emptyList();
        }
        CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(String.join(" ", ns), sender));
        List<String> ret = new ArrayList<>();
        try {
            Suggestions suggestions1 = suggestions.get();
            for (Suggestion suggestion : suggestions1.getList()) {
                ret.add(suggestion.getText());
            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while completing the command.", e);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "sender: " + sender.getName());
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "arguments: " + String.join(" ", ns));
        }
        return ret;
    }


    public void execute(ISender sender, String name, String[] args) {
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = name;
        execute(sender, ns);
    }

    public List<String> tabComplete(ISender sender, String name, String[] args) {
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = name;
        return tabComplete(sender, ns);
    }
}
