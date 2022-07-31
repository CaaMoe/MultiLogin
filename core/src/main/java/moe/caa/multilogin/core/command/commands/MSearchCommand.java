package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;
import moe.caa.multilogin.core.command.argument.YggdrasilIdArgumentType;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MSearchCommand {

    private final CommandHandler handler;

    public MSearchCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("login")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_QUERY_LOGIN))
                        .then(handler.literal("byName")
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(this::executeLoginByName)
                                ))
                        .then(handler.literal("byInGameUUID")
                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                        .executes(this::executeLoginByInGameUUID)
                                )
                        ))
                .then(handler.literal("inGameUUID")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_QUERY_IN_GAME_UUID))
                        .then(handler.literal("byName")
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(this::executeInGameUUIDByName)
                                ))
                        .then(handler.literal("byProfile")
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(this::executeInGameUUIDByProfile)
                                        )
                                )
                        )

                );
    }

    @SneakyThrows
    private int executeInGameUUIDByProfile(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        UUID inGameUUID = CommandHandler.getCore().getSqlManager()
                .getUserDataTable().getInGameUUID(onlineUUID, ysc.getId());

        if (inGameUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_in_game_uuid_by_profile_not_found",
                    new Pair<>("yggdrasil_name", ysc.getName()),
                    new Pair<>("yggdrasil_id", ysc.getId()),
                    new Pair<>("online_uuid", onlineUUID)
            ));
            return 0;
        }
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_in_game_uuid_by_profile",
                new Pair<>("yggdrasil_name", ysc.getName()),
                new Pair<>("yggdrasil_id", ysc.getId()),
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("in_game_uuid", inGameUUID)
        ));
        return 0;
    }

    @SneakyThrows
    private int executeInGameUUIDByName(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID inGameUUID = CommandHandler.getCore().getSqlManager()
                .getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_in_game_uuid_by_username_not_found",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_in_game_uuid_by_username",
                new Pair<>("username", username),
                new Pair<>("in_game_uuid", inGameUUID)
        ));
        return 0;
    }

    private void executeLogin(ISender sender, UUID inGameUUID) throws SQLException {
        Set<Pair<UUID, Integer>> profiles = CommandHandler.getCore().getSqlManager().getUserDataTable().getOnlineProfiles(inGameUUID);
        if (profiles.size() == 0) {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_no_login",
                    new Pair<>("in_game_uuid", inGameUUID)
            ));
            return;
        }
        Map<Integer, List<UUID>> identifiedUUIDMap = new HashMap<>();
        for (Pair<UUID, Integer> profile : profiles) {
            List<UUID> list = identifiedUUIDMap.getOrDefault(profile.getValue2(), new ArrayList<>());
            list.add(profile.getValue1());
            identifiedUUIDMap.put(profile.getValue2(), list);
        }
        String message = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_uuid",
                new Pair<>("list", identifiedUUIDMap.entrySet().stream().map(entry -> {
                    // 获得 Ygg Name
                    String yggName;

                    YggdrasilServiceConfig serviceConfig = CommandHandler.getCore().getPluginConfig().getIdMap().get(entry.getKey());
                    if (serviceConfig == null) {
                        // 不明的
                        yggName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_unknown_entry_name");
                    } else {
                        yggName = serviceConfig.getName();
                    }

                    // 玩家列表
                    String playerListString = entry.getValue().stream()
                            .map(s -> CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_uuid_entry",
                                    new Pair<>("player_name", s)
                            ))
                            .collect(Collectors.joining(
                                            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_player_delimiter")
                                    )
                            );

                    return CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_entry",
                            new Pair<>("yggdrasil_name", yggName),
                            new Pair<>("yggdrasil_id", entry.getKey()),
                            new Pair<>("player_count", entry.getValue().size()),
                            new Pair<>("player_list", playerListString)
                    );
                }).collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_delimiter")))),
                new Pair<>("in_game_uuid", inGameUUID),
                new Pair<>("count", profiles.size())
        );
        sender.sendMessagePL(message);
    }

    @SneakyThrows
    private int executeLoginByInGameUUID(CommandContext<ISender> context) {
        executeLogin(context.getSource(), UUIDArgumentType.getUuid(context, "ingameuuid"));
        return 0;
    }

    @SneakyThrows
    private int executeLoginByName(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID inGameUUID = CommandHandler.getCore().getSqlManager()
                .getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_search_login_by_username_not_found",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        executeLogin(context.getSource(), inGameUUID);
        return 0;
    }
}
