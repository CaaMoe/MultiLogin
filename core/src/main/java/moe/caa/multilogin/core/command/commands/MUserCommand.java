package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.api.plugin.ISender;
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
                                                        .executes(this::executeMergeByName)))))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGE_BYINGAMEUUID))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                                        .executes(c -> 0))))))
                .then(handler.literal("mergeTo")
                        .then(handler.literal("byName")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGETO_BYNAME))
                                .then(handler.argument("username", StringArgumentType.string())
                                        .executes(c -> 0)))
                        .then(handler.literal("byInGameUUID")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_MERGETO_BYINGAMEUUID))
                                .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                        .executes(c -> 0))))
                .then(handler.literal("distribute")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_DISTRIBUTE))
                        .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                        .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                                .executes(c -> 0)))))
                .then(handler.literal("distributeTo")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_DISTRIBUTETO))
                        .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                .executes(c -> 0)))
                .then(handler.literal("remove")
                        .then(handler.literal("profile")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_REMOVE_PROFILE))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(c -> 0))))
                        .then(handler.literal("inGame")
                                .then(handler.literal("byName")
                                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_REMOVE_INGAME_BYNAME))
                                        .then(handler.argument("name", StringArgumentType.string())
                                                .executes(c -> 0)))
                                .then(handler.literal("byInGameUUID")
                                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_USER_REMOVE_INGAME_BYINGAMEUUID))
                                        .then(handler.argument("ingameuuid", UUIDArgumentType.uuid())
                                                .executes(c -> 0)))
                        ));
    }

    private int executeMergeByName(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        String username = StringArgumentType.getString(context, "username");
        return 0;
    }
}
