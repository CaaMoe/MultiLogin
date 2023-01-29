package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;
import moe.caa.multilogin.core.command.argument.YggdrasilIdArgumentType;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;

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
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_OTHER))
                                                .executes(this::executeSetOther)))
                        ))
                .then(handler.literal("settemp")
                        .then(handler.argument("username", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_TEMP_ONESELF))
                                .executes(this::executeSetTempOneself))
                        .then(handler.argument("username", StringArgumentType.string())
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_PROFILE_SET_TEMP_OTHER))
                                                .executes(this::executeSetTempOther)))));
    }

    private int executeSetTempOther(CommandContext<ISender> context) {
        return 0;
    }

    @SneakyThrows
    private int executeSetOther(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        YggdrasilServiceConfig yggdrasilServiceConfig = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_namenonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, yggdrasilServiceConfig.getId())) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_onlinenonexistence",
                            new Pair<>("yggdrasil_name", yggdrasilServiceConfig.getName()),
                            new Pair<>("yggdrasil_id", yggdrasilServiceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID)
                    )
            );
            return 0;
        }
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(onlineUUID, yggdrasilServiceConfig.getId(), gameUUID);
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_succeed",
                            new Pair<>("yggdrasil_name", yggdrasilServiceConfig.getName()),
                            new Pair<>("yggdrasil_id", yggdrasilServiceConfig.getId()),
                            new Pair<>("online_uuid", onlineUUID),
                            new Pair<>("current_username", username)
                    )
            );

            context.getSource().getAsPlayer().kickPlayer(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_succeed_kickmessage",
                            new Pair<>("current_username", username)
                    )
            );
        }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_desc",
                new Pair<>("yggdrasil_name", yggdrasilServiceConfig.getName()),
                new Pair<>("yggdrasil_id", yggdrasilServiceConfig.getId()),
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("current_username", username)
        ), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_other_cq"));
        return 0;
    }

    private int executeSetTempOneself(CommandContext<ISender> context) {
        return 0;
    }

    @SneakyThrows
    private int executeSetOneself(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (gameUUID == null) {
            context.getSource().sendMessagePL(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_set_oneself_nonexistence",
                            new Pair<>("current_username", username)
                    )
            );
            return 0;
        }
        Pair<Pair<UUID, String>, Integer> pair = handler.requireDataCacheArgument(context);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
            CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(pair.getValue1().getValue1(), pair.getValue2(), gameUUID);
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
        if (core.getSqlManager().getInGameProfileTable().getInGameUUID(username) != null) {
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
