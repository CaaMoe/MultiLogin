package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.StringArgumentType;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;
import java.util.regex.Pattern;

public class MRenameCommand {
    private final CommandHandler handler;

    public MRenameCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder
                .then(handler.argument("newname", StringArgumentType.string())
                        .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTILOGIN_RENAME_ONESELF))
                        .executes(this::executeRename))
                .then(handler.argument("newname", StringArgumentType.string())
                        .then(handler.argument("oldname", StringArgumentType.string())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTILOGIN_RENAME_OTHER))
                                .executes(this::executeRenameOther)));
    }

    @SneakyThrows
    private int executeRenameOther(CommandContext<ISender> context) {
        String newname = StringArgumentType.getString(context, "newname");
        String oldname = StringArgumentType.getString(context, "oldname");

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    if (newname.equals(oldname)) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_identical"));
                        return;
                    }
                    String nameAllowedRegular = CommandHandler.getCore().getPluginConfig().getNameAllowedRegular();
                    if (!ValueUtil.isEmpty(nameAllowedRegular)) {
                        if (!Pattern.matches(nameAllowedRegular, newname)) {
                            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_mismatch",
                                    new Pair<>("current_username", newname),
                                    new Pair<>("name_allowed_regular", nameAllowedRegular)
                            ));
                            return;
                        }
                    }
                    UUID gameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUID(oldname);
                    if (gameUUID == null) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_nonexistence",
                                new Pair<>("name", oldname)));
                        return;
                    }
                    try {
                        CommandHandler.getCore().getSqlManager().getInGameProfileTable().updateUsername(gameUUID, newname);
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_succeed",
                                new Pair<>("old_name", oldname),
                                new Pair<>("new_name", newname)));

                        IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(gameUUID);
                        if (player != null) {
                            player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_succeed_kickmessage",
                                    new Pair<>("old_name", player.getName()),
                                    new Pair<>("new_name", newname)));
                        }
                    } catch (SQLIntegrityConstraintViolationException e) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_occupied",
                                new Pair<>("name", newname)));
                        return;
                    }
                    return;
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_desc",
                        new Pair<>("old_name", oldname),
                        new Pair<>("new_name", newname)),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_other_cq",
                        new Pair<>("old_name", oldname)));
        return 0;
    }

    @SneakyThrows
    private int executeRename(CommandContext<ISender> context) {
        handler.requireDataCacheArgument(context);
        IPlayer player = context.getSource().getAsPlayer();
        String newname = StringArgumentType.getString(context, "newname");

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    if (player.getName().equals(newname)) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_identical"));
                        return;
                    }

                    String nameAllowedRegular = CommandHandler.getCore().getPluginConfig().getNameAllowedRegular();
                    if (!ValueUtil.isEmpty(nameAllowedRegular)) {
                        if (!Pattern.matches(nameAllowedRegular, newname)) {
                            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_mismatch",
                                    new Pair<>("current_username", newname),
                                    new Pair<>("name_allowed_regular", nameAllowedRegular)
                            ));
                            return;
                        }
                    }
                    try {
                        CommandHandler.getCore().getSqlManager().getInGameProfileTable().updateUsername(player.getUniqueId(), newname);
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_succeed",
                                new Pair<>("old_name", player.getName()),
                                new Pair<>("new_name", newname)));
                        player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_succeed_kickmessage",
                                new Pair<>("old_name", player.getName()),
                                new Pair<>("new_name", newname)));
                    } catch (SQLIntegrityConstraintViolationException e) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_occupied",
                                new Pair<>("name", newname)));
                    }
                    return;
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_desc",
                        new Pair<>("old_name", player.getName()),
                        new Pair<>("new_name", newname)),
                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_oneself_cq",
                        new Pair<>("old_name", player.getName())));

        return 0;
    }
}
