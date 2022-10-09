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

import java.util.Locale;
import java.util.UUID;

/**
 * /MultiLogin whitelist * 指令处理程序
 */
public class MWhitelistCommand {

    private final CommandHandler handler;

    public MWhitelistCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("add")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_ADD))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeAddUsername)
                        )
                )
                .then(handler.literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_REMOVE))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeRemoveUsername)
                        )
                ).then(handler.literal("permanent")
                        .then(handler.literal("add")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_ADD))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(this::executeAdd)
                                        )
                                )
                        )
                        .then(handler.literal("remove")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_PERMANENT_REMOVE))
                                .then(handler.argument("yggdrasilid", YggdrasilIdArgumentType.yggdrasilid())
                                        .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())
                                                .executes(this::executeRemove)
                                        )
                                )
                        )
                );
    }

    // /MultiLogin whitelist permanent remove <yggdrasilid> <onlineuuid>
    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(onlineUUID, ysc.getId())) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove_repeat",
                    new Pair<>("online_uuid", onlineUUID),
                    new Pair<>("yggdrasil_name", ysc.getName()),
                    new Pair<>("yggdrasil_id", ysc.getId())
            ));
            return 0;
        }
        // 如果有白名单的话，表示有数据，直接更新不需要额外判断
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(onlineUUID, ysc.getId(), false);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove",
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("yggdrasil_name", ysc.getName()),
                new Pair<>("yggdrasil_id", ysc.getId())
        ));
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getUserDataTable().getInGameUUID(onlineUUID, ysc.getId());
        if (inGameUUID != null) {
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
            if (player != null) {
                player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
            }
        }
        return 0;
    }

    // /MultiLogin whitelist permanent add <yggdrasilid> <onlineuuid>
    @SneakyThrows
    private int executeAdd(CommandContext<ISender> context) {
        YggdrasilServiceConfig ysc = YggdrasilIdArgumentType.getYggdrasil(context, "yggdrasilid");
        UUID onlineUUID = UUIDArgumentType.getUuid(context, "onlineuuid");
        if (CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(onlineUUID, ysc.getId())) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add_repeat",
                    new Pair<>("online_uuid", onlineUUID),
                    new Pair<>("yggdrasil_name", ysc.getName()),
                    new Pair<>("yggdrasil_id", ysc.getId())
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(onlineUUID, ysc.getId())) {
            CommandHandler.getCore().getSqlManager().getUserDataTable().insertNewData(onlineUUID, ysc.getId(), null);
        }
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(onlineUUID, ysc.getId(), true);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add",
                new Pair<>("online_uuid", onlineUUID),
                new Pair<>("yggdrasil_name", ysc.getName()),
                new Pair<>("yggdrasil_id", ysc.getId())
        ));
        return 0;
    }

    // /MultiLogin whitelist remove <name>
    @SneakyThrows
    private int executeRemoveUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        int count = 0;
        if (CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().remove(username)) {
            count++;
        }
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID != null) {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID)) {
                count++;
                CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(inGameUUID, false);
            }
        }
        if (count == 0) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove",
                new Pair<>("username", username),
                new Pair<>("count", count)
        ));
        if(inGameUUID != null){
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
            if (player != null) {
                player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
            }
        }
        return 0;
    }

    // /MultiLogin whitelist remove <add>
    @SneakyThrows
    private int executeAddUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        boolean have = false;
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID != null) {
            have = CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
        }
        if (have) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().add(username)) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("username", username)
            ));
            return 0;
        }

        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add",
                new Pair<>("username", username)
        ));
        return 0;
    }
}
