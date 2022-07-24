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

import java.util.Locale;

public class RootCommand {
    private final CommandHandler handler;
    private final MWhitelistCommand mWhitelistCommand;

    public RootCommand(CommandHandler handler) {
        this.handler = handler;
        this.mWhitelistCommand = new MWhitelistCommand(handler);
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("reload")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                        .executes(this::executeReload))
                .then(handler.literal("eraseUsername")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_USERNAME))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeEraseUsername)))
                .then(mWhitelistCommand.register(handler.literal("whitelist")));
    }

    @SneakyThrows
    private int executeEraseUsername(CommandContext<ISender> context) {
        String string = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        int i = handler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
        // 更新前先踢一下
        String kickMsg = handler.getCore().getLanguageHandler().getMessage("in_game_username_occupy",
                new Pair<>("current_username", string));
        // 踢出
        for (IPlayer player : handler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(string)) {
            player.kickPlayer(kickMsg);
        }
        if (i == 0) {
            context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none",
                    new Pair<>("current_username", string)
            ));
        } else {
            context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_erase_username_done",
                    new Pair<>("current_username", string)
            ));
        }
        return 0;
    }

    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        handler.getCore().getPluginConfig().reload();
        context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
        return 0;
    }
}
