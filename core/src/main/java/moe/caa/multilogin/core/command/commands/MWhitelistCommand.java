package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.auth.validate.entry.WhitelistCheckFlows;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.command.argument.UUIDArgumentType;

import java.util.Locale;
import java.util.UUID;

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
                        ).then(handler.argument("yggdrasilid", IntegerArgumentType.integer(0, 127))
                                .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())

                                )
                        )
                )
                .then(handler.literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_REMOVE))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeRemoveUsername)
                        ).then(handler.argument("yggdrasilid", IntegerArgumentType.integer(0, 127))
                                .then(handler.argument("onlineuuid", UUIDArgumentType.uuid())

                                )
                        )
                );
    }

    @SneakyThrows
    private int executeRemoveUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        int count = 0;
        if (WhitelistCheckFlows.cachedWhitelist.remove(username)) {
            count++;
        }
        UUID inGameUUID = handler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID != null) {
            if (handler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID)) {
                count++;
                handler.getCore().getSqlManager().getUserDataTable().setWhitelist(inGameUUID, false);
            }
        }
        if (count == 0) {
            context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat_username",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_username",
                new Pair<>("username", username),
                new Pair<>("count", count)
        ));
        return 0;
    }


    @SneakyThrows
    private int executeAddUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        boolean have = false;
        UUID inGameUUID = handler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(username);
        if (inGameUUID != null) {
            have = handler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
        }
        if (have) {
            context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat_username",
                    new Pair<>("username", username)
            ));
            return 0;
        }
        if (!WhitelistCheckFlows.cachedWhitelist.add(username)) {
            context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat_username",
                    new Pair<>("username", username)
            ));
            return 0;
        }

        context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_username",
                new Pair<>("username", username)
        ));
        return 0;
    }
}
