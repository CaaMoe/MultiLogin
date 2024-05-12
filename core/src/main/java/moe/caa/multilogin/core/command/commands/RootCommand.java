package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.util.*;
import java.util.stream.Collectors;

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
                .then(handler.literal("eraseAllUsernames")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_ALL_USERNAMES))
                        .executes(this::executeEraseAllUsernames))
                .then(handler.literal("confirm")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                        .executes(this::executeConfirm))
                .then(handler.literal("list")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_LIST))
                        .executes(this::executeList))
                .then(new MWhitelistCommand(handler).register(handler.literal("whitelist")))
                .then(new MProfileCommand(handler).register(handler.literal("profile")))
                .then(new MRenameCommand(handler).register(handler.literal("rename")))
                .then(new MFindCommand(handler).register(handler.literal("find")))
                .then(new MInfoCommand(handler).register(handler.literal("info")))
                .then(new MLinkCommand(handler).register(handler.literal("link")))
                .then(new MDataConvert(handler).register(handler.literal("dataconvert")));
    }

    private int executeList(CommandContext<ISender> context) {
        Set<IPlayer> onlinePlayers = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers();

        // service id 分组
        Map<Integer, List<IPlayer>> identifiedPlayerMap = new HashMap<>();
        for (IPlayer player : onlinePlayers) {
            Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());

            int sid = -1;
            if (profile != null) {
                sid = profile.getValue2();
            }
            List<IPlayer> list = identifiedPlayerMap.getOrDefault(sid, new ArrayList<>());
            list.add(player);
            identifiedPlayerMap.put(sid, list);
        }

        CommandHandler.getCore().getPluginConfig().getServiceIdMap().forEach((key, value) -> {
            if (!identifiedPlayerMap.containsKey(key)) {
                identifiedPlayerMap.put(key, new ArrayList<>());
            }
        });

        String message = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list",
                new Pair<>("list", identifiedPlayerMap.entrySet().stream().map(entry -> {
                    // 获得 service name
                    String sname;
                    if (entry.getKey() == -1) {
                        sname = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_unidentified_entry_name");
                    } else {
                        BaseServiceConfig baseServiceConfig = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(entry.getKey());
                        if (baseServiceConfig == null) {
                            sname = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_unknown_entry_name");
                        } else {
                            sname = baseServiceConfig.getName();
                        }
                    }

                    // 玩家列表
                    String playerListString = entry.getValue().stream()
                            .map(s -> CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_player_entry",
                                    new Pair<>("name", s.getName())
                            ))
                            .collect(Collectors.joining(
                                            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_player_delimiter")
                                    )
                            );

                    return CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_entry",
                            new Pair<>("service_name", sname),
                            new Pair<>("service_id", entry.getKey()),
                            new Pair<>("count", entry.getValue().size()),
                            new Pair<>("list", playerListString)
                    );
                }).collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_delimiter")))),
                new Pair<>("count", onlinePlayers.size())
        );
        context.getSource().sendMessagePL(message);
        return 0;
    }


    private int executeEraseAllUsernames(CommandContext<ISender> context) {
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseAllUsername();
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy_all");
                    CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickAll(kickMsg);
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

        UUID ignoreCase = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(string);
        if (ignoreCase == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none",
                    new Pair<>("name", string)
            ));
            return 0;
        }

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy",
                            new Pair<>("name", string));

                    CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(string, kickMsg);
                    if (i == 0) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none",
                                new Pair<>("name", string)
                        ));
                    } else {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_done",
                                new Pair<>("name", string)
                        ));
                    }
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_desc",
                        new Pair<>("name", string)),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_cq",
                        new Pair<>("name", string)));
        return 0;
    }

    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        CommandHandler.getCore().reload();
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
        return 0;
    }
}
