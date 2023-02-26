package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.ServiceIdArgumentType;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class MProfileCommand {

    private final CommandHandler handler;

    public MProfileCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder
                .then(handler.literal("create")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_CREATE))
                        .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(this::executeCreate))))
                .then(handler.literal("set")
                        .then(handler.argument("username", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_ONESELF))
                                .executes(this::executeSetOneself))
                        .then(handler.argument("username", StringArgumentType.string())
                                .then(handler.argument("serviceid", ServiceIdArgumentType.service())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_OTHER))
                                                .executes(this::executeSetOther)))
                        ))
                .then(handler.literal("settemp")
                        .then(handler.argument("username", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_TEMP_ONESELF))
                                .executes(this::executeSetTempOneself))
                        .then(handler.argument("username", StringArgumentType.string())
                                .then(handler.argument("serviceid", ServiceIdArgumentType.service())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_TEMP_OTHER))
                                                .executes(this::executeSetTempOther)))))
                .then(handler.literal("remove")
                        .then(handler.argument("arg", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_REMOVE))
                                .executes(this::executeRemove)));
    }

    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        String arg = StringArgumentType.getString(context, "arg");
        UUID target = ValueUtil.getUuidOrNull(arg);
        if (target != null) {
            if (!CommandHandler.getCore().getSqlManager().getInGameProfileTable().dataExists(target)) {
                context.getSource().sendMessagePL(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_uuid_not_found",
                                new Pair<>("uuid", target)
                        )
                );
                return 0;
            }
        } else {
            target = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(arg);
            if (target == null) {
                context.getSource().sendMessagePL(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_name_not_found",
                                new Pair<>("name", arg)
                        )
                );
                return 0;
            }
        }
        String name = Optional.ofNullable(
                CommandHandler.getCore().getSqlManager().getInGameProfileTable().getUsernameIgnoreIncomplete(target)
        ).orElse(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_unnamed"));

        UUID finalTarget = target;
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getInGameProfileTable().remove(finalTarget);
            Map<Pair<Integer, UUID>, UUID> map = CommandHandler.getCore().getTemplateProfileRedirectHandler().getTemplateProfileRedirectMap();
            map.entrySet().removeIf(e -> e.getValue().equals(finalTarget));

            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_succeed",
                            new Pair<>("name", name),
                            new Pair<>("uuid", finalTarget)
                    ));
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(finalTarget);
            if (player != null) {
                player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_kickmessage"));
            }

        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_desc",
                new Pair<>("name", name),
                new Pair<>("uuid", target)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeSetTempOther(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.getService(context, "serviceid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_namenonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, serviceConfig.getId())) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_onlinenonexistence",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID)
                    )
            );
            return 0;
        }
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getTemplateProfileRedirectHandler().getTemplateProfileRedirectMap().put(
                    new Pair<>(serviceConfig.getId(), onlineUUID),
                    gameUUID
            );
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(onlineUUID, serviceConfig.getId(), gameUUID);
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_succeed",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID),
                            new Pair<>("current_username", username)
                    )
            );

            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(gameUUID);
            if (player != null) {
                player.kickPlayer(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_succeed_kickmessage",
                                new Pair<>("current_username", username)
                        )
                );
            }
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_desc",
                new Pair<>("service_name", serviceConfig.getName()),
                new Pair<>("service_id", serviceConfig.getId()),
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_other_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeSetOther(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.getService(context, "serviceid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_namenonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, serviceConfig.getId())) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_onlinenonexistence",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID)
                    )
            );
            return 0;
        }
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(onlineUUID, serviceConfig.getId(), gameUUID);
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_succeed",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID),
                            new Pair<>("current_username", username)
                    )
            );

            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(gameUUID);
            if (player != null) {
                player.kickPlayer(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_succeed_kickmessage",
                                new Pair<>("current_username", username)
                        )
                );
            }
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_desc",
                new Pair<>("service_name", serviceConfig.getName()),
                new Pair<>("service_id", serviceConfig.getId()),
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeSetTempOneself(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_oneself_nonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        Pair<GameProfile, Integer> pair = handler.requireDataCacheArgument(context);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getTemplateProfileRedirectHandler().getTemplateProfileRedirectMap().put(
                    new Pair<>(pair.getValue2(), pair.getValue1().getId()),
                    gameUUID
            );
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_oneself_succeed",
                            new Pair<>("current_username", username)
                    )
            );

            context.getSource().getAsPlayer().kickPlayer(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_oneself_succeed_kickmessage",
                            new Pair<>("current_username", username)
                    )
            );
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_oneself_desc",
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_temp_oneself_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeSetOneself(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_nonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        Pair<GameProfile, Integer> pair = handler.requireDataCacheArgument(context);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(pair.getValue1().getId(), pair.getValue2(), gameUUID);
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_succeed",
                            new Pair<>("current_username", username)
                    )
            );

            context.getSource().getAsPlayer().kickPlayer(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_succeed_kickmessage",
                            new Pair<>("current_username", username)
                    )
            );
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_desc",
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_cq"));
        return 0;
    }

    @SneakyThrows
    private int executeCreate(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID ingameuuid = UUIDArgumentType.getUuid(context, "ingameuuid");
        MultiCore core = CommandHandler.getCore();
        String nameAllowedRegular = core.getPluginConfig().getNameAllowedRegular();
        if (!ValueUtil.isEmpty(nameAllowedRegular)) {
            if (!Pattern.matches(nameAllowedRegular, username)) {
                context.getSource().sendMessagePL(
                        core.getLanguageHandler().getMessage("command_message_profile_create_namemismatch",
                                new Pair<>("current_username", username),
                                new Pair<>("name_allowed_regular", nameAllowedRegular)
                        )
                );
                return 0;
            }
        }
        if (ingameuuid.version() < 2) {
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_profile_create_uuidmismatch",
                            new Pair<>("uuid", ingameuuid)
                    )
            );
            return 0;
        }
        if (core.getSqlManager().getInGameProfileTable().dataExists(ingameuuid)) {
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_profile_create_uuidoccupied",
                            new Pair<>("uuid", ingameuuid)
                    )
            );
            return 0;
        }
        if (core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username) != null) {
            context.getSource().sendMessagePL(
                    core.getLanguageHandler().getMessage("command_message_profile_create_nameoccupied",
                            new Pair<>("username", username)
                    )
            );
            return 0;
        }
        core.getSqlManager().getInGameProfileTable().insertNewData(ingameuuid, username);
        context.getSource().sendMessagePL(
                core.getLanguageHandler().getMessage("command_message_profile_create",
                        new Pair<>("username", username),
                        new Pair<>("ingameuuid", ingameuuid)
                )
        );
        return 0;
    }
}
