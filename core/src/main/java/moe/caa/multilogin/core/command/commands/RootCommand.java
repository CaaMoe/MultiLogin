package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.util.Locale;
import java.util.Set;

/**
 * /MultiLogin * 指令处理程序和分发程序
 */
public class RootCommand {
    private final CommandHandler handler;

    public RootCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("reload")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                        .executes(this::executeReload))
                .then(handler.literal("eraseUsername")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_USERNAME))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeEraseUsername)))
                .then(handler.literal("eraseAllUsername")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_ALL_USERNAME))
                        .executes(this::executeEraseAllUsername))
                .then(handler.literal("current")
                        .then(handler.argument("username", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CURRENT_OTHER))
                                .executes(this::executeCurrentOther))
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CURRENT_ONESELF))
                        .executes(this::executeCurrentOneself))
                .then(handler.literal("confirm")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                        .executes(this::executeConfirm))
                .then(new MWhitelistCommand(handler).register(handler.literal("whitelist")))
                .then(new MProfileCommand(handler).register(handler.literal("profile")))
                .then(new MRenameCommand(handler).register(handler.literal("rename")));
    }

    private int executeCurrentOther(CommandContext<ISender> context) throws CommandSyntaxException {
        String username = StringArgumentType.getString(context, "username");
        Set<IPlayer> players = handler.requirePlayersArgument(username);
        if (players.size() > 1) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_multi",
                    new Pair<>("count", players.size())
            ));
        }
        for (IPlayer player : players) {
            Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
            if (profile == null) {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_unknown"));
            } else {
                String yggName;
                BaseServiceConfig ysc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profile.getValue2());
                if (ysc == null) {
                    yggName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_unidentified_name");
                } else {
                    yggName = ysc.getName();
                }
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other",
                        new Pair<>("in_game_username", player.getName()),
                        new Pair<>("in_game_uuid", player.getUniqueId()),
                        new Pair<>("service_name", yggName),
                        new Pair<>("service_id", profile.getValue2()),
                        new Pair<>("online_username", profile.getValue1().getName()),
                        new Pair<>("online_uuid", profile.getValue1().getId())
                ));
            }
        }
        return 0;
    }

    private int executeCurrentOneself(CommandContext<ISender> context) throws CommandSyntaxException {
        Pair<GameProfile, Integer> profile = handler.requireDataCacheArgument(context);

        String yggName;
        BaseServiceConfig ysc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profile.getValue2());
        if (ysc == null) {
            yggName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_oneself_unidentified_name");
        } else {
            yggName = ysc.getName();
        }

        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_oneself",
                new Pair<>("in_game_username", context.getSource().getAsPlayer().getName()),
                new Pair<>("in_game_uuid", context.getSource().getAsPlayer().getUniqueId()),
                new Pair<>("service_name", yggName),
                new Pair<>("service_id", profile.getValue2()),
                new Pair<>("online_username", profile.getValue1().getName()),
                new Pair<>("online_uuid", profile.getValue1().getId())
        ));
        return 0;
    }

    private int executeEraseAllUsername(CommandContext<ISender> context) {
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseAllUsername();
                    // 更新前先踢一下
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy_all");
                    // 踢出
                    for (IPlayer player : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers()) {
                        player.kickPlayer(kickMsg);
                    }
                    context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_done",
                            new Pair<>("count", i)
                    ));
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_desc"),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_cq"));
        return 0;
    }

    // /MultiLogin confirm
    @SneakyThrows
    private int executeConfirm(CommandContext<ISender> context) {
        handler.getSecondaryConfirmationHandler().confirm(context.getSource());
        return 0;
    }

    // /MultiLogin eraseUsername <name>
    @SneakyThrows
    private int executeEraseUsername(CommandContext<ISender> context) {
        String string = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
                    // 更新前先踢一下
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy",
                            new Pair<>("current_username", string));
                    // 踢出
                    for (IPlayer player : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(string)) {
                        player.kickPlayer(kickMsg);
                    }
                    if (i == 0) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none",
                                new Pair<>("current_username", string)
                        ));
                    } else {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_done",
                                new Pair<>("current_username", string)
                        ));
                    }
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_desc",
                        new Pair<>("username", string)),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_cq"));
        return 0;
    }

    // /MultiLogin reload
    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        CommandHandler.getCore().reload();
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
        return 0;
    }
}
