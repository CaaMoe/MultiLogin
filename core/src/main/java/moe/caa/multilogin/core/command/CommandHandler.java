package moe.caa.multilogin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import moe.caa.multilogin.api.command.CommandAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.commands.RootCommand;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 中央命令处理程序
 */
public class CommandHandler implements CommandAPI {
    @Getter
    private static MultiCore core;
    @Getter
    private static BuiltInExceptions builtInExceptions;
    private final CommandDispatcher<ISender> dispatcher;
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
        execute(sender, String.join(" ", args));
    }

    @Override
    public void execute(ISender sender, String args) {
        core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                dispatcher.execute(args, sender);
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
        return tabComplete(sender, String.join(" ", args));
    }

    @Override
    public List<String> tabComplete(ISender sender, String args) {
        if (!sender.hasPermission(Permissions.COMMAND_TAB_COMPLETE)) {
            return Collections.emptyList();
        }
        CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(args, sender));
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

    /**
     * 子命令名字
     */
    public final LiteralArgumentBuilder<ISender> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    /**
     * 构建命令参数
     */
    public final <T> RequiredArgumentBuilder<ISender, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    /**
     * 检查玩家执行
     */
    public final void requirePlayer(CommandContext<ISender> context) throws CommandSyntaxException {
        if (!context.getSource().isPlayer()) {
            throw builtInExceptions.requirePlayer().create();
        }
    }

    /**
     * 检查输入玩家参数
     */
    public final Set<IPlayer> requirePlayersArgument(CommandContext<ISender> context, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(context, name);
        Set<IPlayer> players = core.getPlugin().getRunServer().getPlayerManager().getPlayers(string);
        if (players.size() == 0)
            throw builtInExceptions.playerNotOnline().create(string);

        return players;
    }

    /**
     * 检查是通过猫踢螺钉登录的玩家
     */
    public final Pair<UUID, Integer> requireDataCacheArgument(CommandContext<ISender> context) throws CommandSyntaxException {
        requirePlayer(context);
        Pair<UUID, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(context.getSource().getAsPlayer().getUniqueId());
        if (profile == null) {
            throw builtInExceptions.cacheNotFoundSelf().create();
        }
        return profile;
    }
}
