package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
                                                        .executes(c -> 0)))))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGE_BYINGAMEUUID))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                                        .executes(c -> 0))))))
                .then(handler.literal("mergeMe")
                        .then(handler.literal("byName")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGEME_BYNAME))
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(c -> 0)))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGEME_BYINGAMEUUID))
                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                        .executes(c -> 0))))
                .then(handler.literal("remove")
                        .then(handler.literal("profile")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_REMOVE_PROFILE))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(this::removeProfile)))));
    }

    private int removeProfile(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, ysc.getId())) {
                CommandHandler.getCore().getSqlManager().getUserDataTable().delete(onlineUUID, ysc.getId());
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_user_remove_profile_done",
                        new Pair<>("yggdrasil_name", ysc.getName()),
                        new Pair<>("yggdrasil_id", ysc.getId()),
                        new Pair<>("online_uuid", onlineUUID)));

                UUID inGameUUID = CommandHandler.getCore().getCache().getInGameUUID(onlineUUID, ysc.getId());
                if (inGameUUID != null) {
                    IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
                    if (player != null) {
                        player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_profile_remove"));
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