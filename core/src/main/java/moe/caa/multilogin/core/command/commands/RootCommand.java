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

/**
 * /MultiLogin * 指令处理程序和分发程序
 */
public class RootCommand {
    private final CommandHandler handler;
    private final MWhitelistCommand mWhitelistCommand;
    private final MSearchCommand mSearchCommand;
    private final MUserCommand mUserCommand;
    private final MRenameCommand mRenameCommand;

    public RootCommand(CommandHandler handler) {
        this.handler = handler;
        this.mWhitelistCommand = new MWhitelistCommand(handler);
        this.mSearchCommand = new MSearchCommand(handler);
        this.mUserCommand = new MUserCommand(handler);
        this.mRenameCommand = new MRenameCommand(handler);
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder.then(handler.literal("reload")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                        .executes(this::executeReload))
                .then(handler.literal("eraseUsername")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_USERNAME))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeEraseUsername)))
                .then(handler.literal("eraseAllUsername")
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_ERASE_ALL_USERNAME))
                        .executes(this::executeEraseAllUsername))
                .then(handler.literal("confirm")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                        .executes(this::executeConfirm))
                .then(mSearchCommand.register(handler.literal("search")))
                .then(mUserCommand.register(handler.literal("user")))
                .then(mWhitelistCommand.register(handler.literal("whitelist")))
                .then(mRenameCommand.register(handler.literal("rename")));
    }

    private int executeEraseAllUsername(CommandContext<ISender> context) {
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseAllUsername();
                    // 更新前先踢一下
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy_all");
                    // 踢出
                    for (IPlayer player : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers()) {
                        player.kickPlayer(kickMsg);
                    }
                    context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_done",
                            new Pair<>("count", i)
                    ));
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_desc"),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_cq"));
        return 0;
    }

    // /MultiLogin confirm
    @SneakyThrows
    private int executeConfirm(CommandContext<ISender> context) {
        handler.getSecondaryConfirmationHandler().confirm(context.getSource());
        return 0;
    }

    // /MultiLogin eraseUsername <name>
    @SneakyThrows
    private int executeEraseUsername(CommandContext<ISender> context) {
        String string = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
                    // 更新前先踢一下
                    String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy",
                            new Pair<>("current_username", string));
                    // 踢出
                    for (IPlayer player : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(string)) {
                        player.kickPlayer(kickMsg);
                    }
                    if (i == 0) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none",
                                new Pair<>("current_username", string)
                        ));
                    } else {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_done",
                                new Pair<>("current_username", string)
                        ));
                    }
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_desc",
                        new Pair<>("username", string)),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_cq"));
        return 0;
    }

    // /MultiLogin reload
    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        CommandHandler.getCore().reload();
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
        return 0;
    }
}
