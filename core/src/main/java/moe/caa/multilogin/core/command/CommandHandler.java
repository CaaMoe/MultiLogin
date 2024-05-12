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
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.command.CommandAPI;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.command.commands.RootCommand;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                LoggerProvider.getLogger().debug(String.format("An expected exception occurs when the %s command is executed.", String.join(" ", args)), e);
            } catch (Exception e) {
                sender.sendMessagePL(core.getLanguageHandler().getMessage("command_error"));
                LoggerProvider.getLogger().error(String.format("An exception occurs when the %s command is executed.", String.join(" ", args)), e);
            }
        });
    }

    @Override
    public List<String> tabComplete(ISender sender, String[] args) {
        if (args.length == 1) {
            return tabComplete(sender, args[0] + " ");
        }
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

    public final void requirePlayerAndNoSelf(CommandContext<ISender> context, IPlayer player) throws CommandSyntaxException {
        if (!context.getSource().isPlayer()) {
            throw builtInExceptions.requirePlayer().create();
        }
        if(context.getSource().getAsPlayer().getUniqueId().equals(player.getUniqueId())){
            throw builtInExceptions.noSelf().create();
        }
    }

    /**
     * 检查是通过猫踢螺钉登录的玩家
     */
    public final Pair<GameProfile, Integer> requireDataCacheArgumentSelf(CommandContext<ISender> context) throws CommandSyntaxException {
        requirePlayer(context);
        Pair<GameProfile, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(context.getSource().getAsPlayer().getUniqueId());
        if (profile == null) {
            throw builtInExceptions.cacheNotFoundSelf().create();
        }
        return profile;
    }

    public final Pair<GameProfile, Integer> requireDataCacheArgumentOther(IPlayer player) throws CommandSyntaxException {
        Pair<GameProfile, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
        if (profile == null) {
            throw builtInExceptions.cacheNotFoundOther().create(player.getUniqueId(), player.getName());
        }
        return profile;
    }
}
