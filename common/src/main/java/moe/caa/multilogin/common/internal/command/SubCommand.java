package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.OnlinePlayer;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import moe.caa.multilogin.common.internal.util.EditableMiniMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class SubCommand<S> {
    public final List<CommandDescription> commandDescriptions = new ArrayList<>();
    public final CommandManager<S> manager;

    public SubCommand(CommandManager<S> manager) {
        this.manager = manager;
    }

    public abstract void register(ArgumentBuilder<S, ?> builder);

    protected void addCommandDescription(
            String label,
            String permission,
            EditableMiniMessage description
    ) {
        commandDescriptions.add(new CommandDescription(label, permission, description));
    }

    protected SuggestionProvider<S> suggestWith(Supplier<Collection<String>> listSupplier) {
        return (context, builder) -> {
            CompletableFuture<Suggestions> future = new CompletableFuture<>();
            manager.core.virtualPerTaskExecutor.execute(() -> {
                for (String s : listSupplier.get()) {
                    if (s.toLowerCase().contains(builder.getRemaining())) {
                        builder.suggest(s);
                    }
                }
                future.complete(builder.build());
            });
            return future;
        };
    }

    protected SuggestionProvider<S> suggestWith(Function<String, List<String>> function) {
        return (context, builder) -> {
            CompletableFuture<Suggestions> future = new CompletableFuture<>();
            manager.core.virtualPerTaskExecutor.execute(() -> {
                function.apply(builder.getRemaining());
                future.complete(builder.build());
            });
            return future;
        };
    }

    protected SuggestionProvider<S> suggestWithOnlinePlayer() {
        suggestWith(() -> manager.core.platform.getOnlinePlayerManager().getOnlinePlayers().keySet());
    }

    protected LiteralArgumentBuilder<S> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public <T> RequiredArgumentBuilder<S, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected Predicate<S> predicateHasPermission(String permission) {
        return sender -> hasPermission(sender, permission);
    }

    protected boolean hasPermission(S s, String permission) {
        return manager.wrapSender(s).hasPermission(permission);
    }

    protected void resolveOnlinePlayerRunOrElseTip(S s, String target, Consumer<OnlinePlayer> consumer) {
        Sender sender = manager.wrapSender(s);
        OnlinePlayer player = manager.core.platform.getOnlinePlayerManager().getPlayerExactByName(target);
        if (player != null) {
            consumer.accept(player);
        } else {
            sender.sendMessage(manager.core.messageConfig.commandGeneralArgumentOnlinePlayerNotFound.get()
                    .replace("<target>", target)
                    .build());
        }
    }

    protected void ifOnlinePlayerRunOrElseTip(Sender sender, Consumer<OnlinePlayer> consumer) {
        if (sender instanceof OnlinePlayer player) {
            consumer.accept(player);
        } else {
            sender.sendMessage(manager.core.messageConfig.commandGeneralRequiredPlayer.get().build());
        }
    }

    protected void ifReconnectFeatureEnableRunOrElseTip(Sender sender, Runnable runnable) {
        if (!manager.core.mainConfig.reconnectFeature.get().enable.get()) {
            sender.sendMessage(manager.core.messageConfig.commandGeneralReconnectFeatureNotEnabled.get().build());
        } else {
            runnable.run();
        }
    }

    protected void ifFetchMeOnlineDataRunOrElseTip(Sender sender, OnlinePlayer player, Consumer<OnlineData> consumer) {
        OnlineData onlineData = player.getOnlineData();
        if (onlineData == null) {
            sender.sendMessage(manager.core.messageConfig.commandGeneralNotFoundOnlineDataMe.get().build());
            return;
        }
        consumer.accept(onlineData);
    }

    protected void ifFetchOtherOnlineDataRunOrElseTip(Sender sender, OnlinePlayer player, Consumer<OnlineData> consumer) {
        OnlineData onlineData = player.getOnlineData();
        if (onlineData == null) {
            sender.sendMessage(manager.core.messageConfig.commandGeneralNotFoundOnlineDataTarget.get()
                    .replace("<target>", player.getName())
                    .build()
            );
            return;
        }
        consumer.accept(onlineData);
    }

    public record CommandDescription(
            String label,
            String permission,
            EditableMiniMessage description
    ) {
    }
}
