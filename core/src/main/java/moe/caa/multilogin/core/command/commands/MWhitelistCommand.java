package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlineArgumentType;
import moe.caa.multilogin.core.command.argument.StringArgumentType;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /MultiLogin whitelist * 指令处理程序
 */
public class MWhitelistCommand {

    private final CommandHandler handler;

    public MWhitelistCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder
                .then(handler.literal("add")
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
                ).then(handler.literal("specific")
                        .then(handler.literal("add")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_SPECIFIC_ADD))
                                .then(handler.argument("online", OnlineArgumentType.online())
                                        .executes(this::executeAdd)

                                )
                        )
                        .then(handler.literal("remove")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_SPECIFIC_REMOVE))
                                .then(handler.argument("online", OnlineArgumentType.online())
                                        .executes(this::executeRemove)
                                )
                        )
                ).then(handler.literal("list")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_LIST))
                        .executes(this::executeList)
                        .then(handler.literal("verbose")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_LIST_VERBOSE))
                                .executes(this::executeListVerbose)
                        )
                );
    }

    // /MultiLogin whitelist permanent remove <serviceid> <onlineuuid>
    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
        if (!online.isWhitelist()) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove_repeat",
                    new Pair<>("online_uuid", online.getOnlineUUID()),
                    new Pair<>("online_name", online.getOnlineName()),
                    new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                    new Pair<>("service_id", online.getBaseServiceConfig().getId())
            ));
            return 0;
        }
        // 如果有白名单的话，表示有数据，直接更新不需要额外判断
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), false);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove",
                new Pair<>("online_uuid", online.getOnlineUUID()),
                new Pair<>("online_name", online.getOnlineName()),
                new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                new Pair<>("service_id", online.getBaseServiceConfig().getId())
        ));
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getUserDataTable().getInGameUUID(online.getOnlineUUID(), online.getBaseServiceConfig().getId());
        if (inGameUUID != null) {
            CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(inGameUUID, CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
        }
        return 0;
    }

    // /MultiLogin whitelist permanent add <serviceid> <onlineuuid>
    @SneakyThrows
    private int executeAdd(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
        if (online.isWhitelist()) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add_repeat",
                    new Pair<>("online_uuid", online.getOnlineUUID()),
                    new Pair<>("online_name", online.getOnlineName()),
                    new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                    new Pair<>("service_id", online.getBaseServiceConfig().getId())
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(online.getOnlineUUID(), online.getBaseServiceConfig().getId())) {
            CommandHandler.getCore().getSqlManager().getUserDataTable().insertNewData(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), null, null);
        }
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), true);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add",
                new Pair<>("online_uuid", online.getOnlineUUID()),
                new Pair<>("online_name", online.getOnlineName()),
                new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                new Pair<>("service_id", online.getBaseServiceConfig().getId())
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
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (inGameUUID != null) {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID)) {
                count++;
                CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(inGameUUID, false);
            }
        }
        if (count == 0) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove",
                new Pair<>("name", username),
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

    // /MultiLogin whitelist add <name>
    @SneakyThrows
    private int executeAddUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        boolean have = false;
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (inGameUUID != null) {
            have = CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
        }
        if (have) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().add(username)) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }

        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add",
                new Pair<>("name", username)
        ));
        return 0;
    }

    // /MultiLogin whitelist list [verbose]
    @SneakyThrows
    private int executeList(CommandContext<ISender> context, boolean verbose) {
        List<String> list = CommandHandler.getCore().getSqlManager().getUserDataTable().listWhitelist(verbose);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage(
            "command_message_whitelist_list_table",
            new Pair<>("count", list.size()),
            new Pair<>("list", String.join(verbose ? ", \n" : ", ", list))
        ));


        var cache = CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist();
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage(
            "command_message_whitelist_list_cache",
            new Pair<>("list", cache.stream().collect(Collectors.joining(", "))),
            new Pair<>("count", cache.size())
        ));

        return 0;
    }

    @SneakyThrows
    private int executeList(CommandContext<ISender> context) {
        return this.executeList(context, false);
    }

    @SneakyThrows
    private int executeListVerbose(CommandContext<ISender> context) {
        return this.executeList(context, true);
    }
}
