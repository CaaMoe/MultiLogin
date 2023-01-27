package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * /MultiLogin * 指令处理程序和分发程序
 */
public class RootCommand {
    private final CommandHandler handler;
    private final MWhitelistCommand mWhitelistCommand;
    private final MRenameCommand mRenameCommand;

    public RootCommand(CommandHandler handler) {
        this.handler = handler;
        this.mWhitelistCommand = new MWhitelistCommand(handler);
        this.mRenameCommand = new MRenameCommand(handler);
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
                .then(handler.literal("loginto")
                        .then(handler.argument("username", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LOGIN_TO_ONESELF))
                                .executes(this::executeLoginToOneself)))
                .then(handler.literal("confirm")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                        .executes(this::executeConfirm))
                .then(handler.literal("createProfile")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CREATE_PROFILE))
                        .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(this::executeCreateProfile))))
                .then(mWhitelistCommand.register(handler.literal("whitelist")))
                .then(mRenameCommand.register(handler.literal("rename")));
    }

    @SneakyThrows
    private int executeLoginToOneself(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_loginto_oneself_nonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        Pair<Pair<UUID, String>, Integer> pair = handler.requireDataCacheArgument(context);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(pair.getValue1().getValue1(), pair.getValue2(), gameUUID);
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_loginto_oneself_succeed",
                            new Pair<>("current_username", username)
                    )
            );

            context.getSource().getAsPlayer().kickPlayer(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_loginto_oneself_succeed_kickmessage",
                            new Pair<>("current_username", username)
                    )
            );
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_loginto_oneself_desc",
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_loginto_oneself_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeCreateProfile(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID ingameuuid = UUIDArgumentType.getUuid(context, "ingameuuid");
        MultiCore core = CommandHandler.getCore();
        String nameAllowedRegular = core.getPluginConfig().getNameAllowedRegular();
        if (!ValueUtil.isEmpty(nameAllowedRegular)) {
            if (!Pattern.matches(nameAllowedRegular, username)) {
                context.getSource().sendMessagePL(
                        core.getLanguageHandler().getMessage("command_message_createprofile_namemismatch",
                                new Pair<>("current_username", username),
                                new Pair<>("name_allowed_regular", nameAllowedRegular)
                        )
                );
                return 0;
            }
        }
        if (ingameuuid.version() < 2) {
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_createprofile_uuidmismatch",
                            new Pair<>("uuid", ingameuuid)
                    )
            );
            return 0;
        }
        if (core.getSqlManager().getInGameProfileTable().dataExists(ingameuuid)) {
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_createprofile_uuidoccupied",
                            new Pair<>("uuid", ingameuuid)
                    )
            );
            return 0;
        }
        if (core.getSqlManager().getInGameProfileTable().getInGameUUID(username) != null){
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_createprofile_nameoccupied",
                            new Pair<>("username", username)
                    )
            );
            return 0;
        }
        core.getSqlManager().getInGameProfileTable().insertNewData(ingameuuid, username);
        context.getSource().sendMessagePL(
                core.getLanguageHandler().getMessage("command_message_createprofile",
                        new Pair<>("username", username),
                        new Pair<>("ingameuuid", ingameuuid)
                )
        );

        return 0;
    }

    private int executeCurrentOther(CommandContext<ISender> context) throws CommandSyntaxException {
        Set<IPlayer> players = handler.requirePlayersArgument(context, "username");
        if (players.size() > 1) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_multi",
                    new Pair<>("count", players.size())
            ));
        }
        for (IPlayer player : players) {
            Pair<Pair<UUID, String>, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
            if (profile == null) {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_unknown"));
            } else {
                String yggName;
                YggdrasilServiceConfig ysc = CommandHandler.getCore().getPluginConfig().getIdMap().get(profile.getValue2());
                if (ysc == null) {
                    yggName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other_unidentified_name");
                } else {
                    yggName = ysc.getName();
                }
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_other",
                        new Pair<>("in_game_username", player.getName()),
                        new Pair<>("in_game_uuid", player.getUniqueId()),
                        new Pair<>("yggdrasil_name", yggName),
                        new Pair<>("yggdrasil_id", profile.getValue2()),
                        new Pair<>("online_username", profile.getValue1().getValue2()),
                        new Pair<>("online_uuid", profile.getValue1().getValue1())
                ));
            }
        }
        return 0;
    }

    private int executeCurrentOneself(CommandContext<ISender> context) throws CommandSyntaxException {
        Pair<Pair<UUID, String>, Integer> profile = handler.requireDataCacheArgument(context);

        String yggName;
        YggdrasilServiceConfig ysc = CommandHandler.getCore().getPluginConfig().getIdMap().get(profile.getValue2());
        if (ysc == null) {
            yggName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_oneself_unidentified_name");
        } else {
            yggName = ysc.getName();
        }

        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_current_oneself",
                new Pair<>("in_game_username", context.getSource().getAsPlayer().getName()),
                new Pair<>("in_game_uuid", context.getSource().getAsPlayer().getUniqueId()),
                new Pair<>("yggdrasil_name", yggName),
                new Pair<>("yggdrasil_id", profile.getValue2()),
                new Pair<>("online_username", profile.getValue1().getValue2()),
                new Pair<>("online_uuid", profile.getValue1().getValue1())
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
