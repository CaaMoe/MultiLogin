package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;
import moe.caa.multilogin.core.command.argument.YggdrasilIdArgumentType;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

import java.util.UUID;

/**
 * /MultiLogin user * 指令处理程序
 */
public class MUserCommand {
    private final CommandHandler handler;

    public MUserCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("merge")
                        .then(handler.literal("byName")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGE_BYNAME))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .then(handler.argument("username", StringArgumentType.string())
                                                        .executes(this::executeMergeByName)))))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGE_BYINGAMEUUID))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeMergeByInGameUUID))))))
                .then(handler.literal("mergeMe")
                        .then(handler.literal("byName")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGEME_BYNAME))
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(this::executeMergeMeByName)))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGEME_BYINGAMEUUID))
                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                        .executes(this::executeMergeMeByInGameUUID))))
                .then(handler.literal("remove")
                        .then(handler.literal("profile")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_REMOVE_PROFILE))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(this::executeRemoveProfile)))));
    }

    // /MultiLogin user merge * 集中处理
    private void executeMerge(ISender sender, YggdrasilServiceConfig ysc, UUID onlineUUID, UUID inGameUUID) {
        handler.getSecondaryConfirmationHandler().submit(sender, () -> {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, ysc.getId())) {
                CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(onlineUUID, ysc.getId(), inGameUUID);
                sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_done",
                        new Pair<>("yggdrasil_name", ysc.getName()),
                        new Pair<>("yggdrasil_id", ysc.getId()),
                        new Pair<>("online_uuid", onlineUUID),
                        new Pair<>("in_game_uuid", inGameUUID)));

                UUID needKick = CommandHandler.getCore().getPlayerHandler().getInGameUUID(onlineUUID, ysc.getId());
                if (needKick != null) {
                    IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(needKick);
                    if (player != null) {
                        player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_profile_merged"));
                    }
                }
            } else {
                sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_profile_not_found",
                        new Pair<>("yggdrasil_name", ysc.getName()),
                        new Pair<>("yggdrasil_id", ysc.getId()),
                        new Pair<>("online_uuid", onlineUUID)
                ));
            }
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_desc",
                new Pair<>("yggdrasil_name", ysc.getName()),
                new Pair<>("yggdrasil_id", ysc.getId()),
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("in_game_uuid", inGameUUID)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_cq"));
    }

    // /MultiLogin user merge byInGameUUID <yggdrasilId> <onlineuuid> <ingameuuid>
    private int executeMergeByInGameUUID(CommandContext<ISender> context) {
        YggdrasilServiceConfig yggdrasilid = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        UUID inGameUUID = UUIDArgumentType.getUuid(context, "ingameuuid");
        executeMerge(context.getSource(), yggdrasilid, onlineUUID, inGameUUID);
        return 0;
    }

    // /MultiLogin user merge byName <yggdrasilId> <onlineuuid> <name>
    @SneakyThrows
    private int executeMergeByName(CommandContext<ISender> context) {
        YggdrasilServiceConfig yggdrasilid = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        String username = StringArgumentType.getString(context, "username");
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_ingameuuid_not_found",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        executeMerge(context.getSource(), yggdrasilid, onlineUUID, inGameUUID);
        return 0;
    }

    // /MultiLogin user mergeMe byInGameUUID <ingameuuid>
    @SneakyThrows
    private int executeMergeMeByInGameUUID(CommandContext<ISender> context) {
        Pair<UUID, Integer> uuidIntegerPair = handler.requireDataCacheArgument(context);
        UUID inGameUUID = UUIDArgumentType.getUuid(context, "ingameuuid");
        YggdrasilServiceConfig yggdrasilServiceConfig = CommandHandler.getCore().getPluginConfig().getIdMap().get(uuidIntegerPair.getValue2());
        if (yggdrasilServiceConfig == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_mergeme_yggdrasil_not_found"));
            return 0;
        }
        executeMerge(context.getSource(), yggdrasilServiceConfig, uuidIntegerPair.getValue1(), inGameUUID);
        return 0;
    }

    // /MultiLogin user mergeMe byName <name>
    @SneakyThrows
    private int executeMergeMeByName(CommandContext<ISender> context) {
        Pair<UUID, Integer> uuidIntegerPair = handler.requireDataCacheArgument(context);
        String username = StringArgumentType.getString(context, "username");
        YggdrasilServiceConfig yggdrasilServiceConfig = CommandHandler.getCore().getPluginConfig().getIdMap().get(uuidIntegerPair.getValue2());
        if (yggdrasilServiceConfig == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_mergeme_yggdrasil_not_found"));
            return 0;
        }
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID == null) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_merge_ingameuuid_not_found",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        executeMerge(context.getSource(), yggdrasilServiceConfig, uuidIntegerPair.getValue1(), inGameUUID);
        return 0;
    }

    // /MultiLogin user remove profile <yggdrasilId> <onlineuuid>
    private int executeRemoveProfile(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, ysc.getId())) {
                CommandHandler.getCore().getSqlManager().getUserDataTable().delete(onlineUUID, ysc.getId());
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_remove_profile_done",
                        new Pair<>("yggdrasil_name", ysc.getName()),
                        new Pair<>("yggdrasil_id", ysc.getId()),
                        new Pair<>("online_uuid", onlineUUID)));

                UUID inGameUUID = CommandHandler.getCore().getPlayerHandler().getInGameUUID(onlineUUID, ysc.getId());
                if (inGameUUID != null) {
                    IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
                    if (player != null) {
                        player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_profile_removed"));
                    }
                }
            } else {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_remove_profile_not_found",
                        new Pair<>("yggdrasil_name", ysc.getName()),
                        new Pair<>("yggdrasil_id", ysc.getId()),
                        new Pair<>("online_uuid", onlineUUID)));
            }
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_remove_profile_desc",
                new Pair<>("yggdrasil_name", ysc.getName()),
                new Pair<>("yggdrasil_id", ysc.getId()),
                new Pair<>("online_uuid", onlineUUID)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_remove_profile_cq"));
        return 0;
    }
}