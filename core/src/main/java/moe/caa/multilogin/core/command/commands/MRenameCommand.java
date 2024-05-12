package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.internal.plugin.ISender;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.ProfileArgumentType;
import moe.caa.multilogin.core.command.argument.StringArgumentType;

import java.sql.SQLIntegrityConstraintViolationException;
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
                        .then(handler.argument("profile", ProfileArgumentType.profile())
                                .requires(iSender -> iSender.hasPermission(Permissions.COMMAND_MULTILOGIN_RENAME_OTHER))
                                .executes(this::executeRenameOther)));
    }

    @SneakyThrows
    private int executeRenameOther(CommandContext<ISender> context) {
        String newname = StringArgumentType.getString(context, "newname");
        ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");

        processRename(context, newname, profile);
        return 0;
    }

    @SneakyThrows
    private int executeRename(CommandContext<ISender> context) {
        String newname = StringArgumentType.getString(context, "newname");
        handler.requireDataCacheArgumentSelf(context);

        processRename(context, newname, new ProfileArgumentType.ProfileArgument(context.getSource().getAsPlayer().getUniqueId(), context.getSource().getAsPlayer().getName()));
        return 0;
    }

    private void processRename(CommandContext<ISender> context, String newName, ProfileArgumentType.ProfileArgument argument) {
        if (newName.equals(argument.getProfileName())) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_identical"));
            return;
        }
        String nameAllowedRegular = CommandHandler.getCore().getPluginConfig().getNameAllowedRegular();
        if (!ValueUtil.isEmpty(nameAllowedRegular)) {
            if (!Pattern.matches(nameAllowedRegular, newName)) {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_mismatch",
                        new Pair<>("name", newName),
                        new Pair<>("regular", nameAllowedRegular)
                ));
                return;
            }
        }

        handler.getSecondaryConfirmationHandler().submit(context.getSource(), () -> {
                    try {
                        CommandHandler.getCore().getSqlManager().getInGameProfileTable().updateUsername(argument.getProfileUUID(), newName);
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_succeed",
                                new Pair<>("profile_name", argument.getProfileName()),
                                new Pair<>("new_name", newName),
                                new Pair<>("profile_uuid", argument.getProfileUUID()))
                        );

                        CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(argument.getProfileUUID(), (CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_succeed_kickmessage",
                                new Pair<>("profile_name", argument.getProfileName()),
                                new Pair<>("new_name", newName),
                                new Pair<>("profile_uuid", argument.getProfileUUID()))));
                    } catch (SQLIntegrityConstraintViolationException e) {
                        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_occupied",
                                new Pair<>("name", newName)));
                    }
                }, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_desc",
                        new Pair<>("profile_name", argument.getProfileName()),
                        new Pair<>("new_name", newName),
                        new Pair<>("profile_uuid", argument.getProfileUUID())),

                CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_cq",
                        new Pair<>("profile_name", argument.getProfileName()),
                        new Pair<>("new_name", newName),
                        new Pair<>("profile_uuid", argument.getProfileUUID()))
        );
    }
}
