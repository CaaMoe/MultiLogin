package moe.caa.multilogin.core.command.commands.subcommands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.arguments.UUIDArgumentType;
import moe.caa.multilogin.core.command.arguments.UserdataByOnlineUUIDArgumentType;
import moe.caa.multilogin.core.command.arguments.YggdrasilServiceArgumentType;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.UUID;

public class MultiLoginUserdataCommand extends BaseSubCommand {
    public MultiLoginUserdataCommand(MultiCore core) {
        super(core);
    }

    @Override
    public ArgumentBuilder<ISender, ?> getSubExecutor() {
        return literal("userdata")
                .then(literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_USERDATA_REMOVE))
                        .then(argument("user", UserdataByOnlineUUIDArgumentType.user())
                                .executes(this::executeRemove)
                        )
                )
                .then(literal("info")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_USERDATA_INFO))
                        .then(argument("user", UserdataByOnlineUUIDArgumentType.user())
                                .executes(this::executeInfo)
                        )
                )
                .then(literal("modify")
                        .then(literal("yggdrasil")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_USERDATA_MODIFY_YGGDRASIL))
                                .then(argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                        .then(argument("user", UserdataByOnlineUUIDArgumentType.user())
                                                .executes(this::executeModifyYggdrasil)
                                        )
                                )
                        )
                        .then(literal("redirectUuid")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_USERDATA_MODIFY_REDIRECT_UUID))
                                .then(argument("uuid", UUIDArgumentType.uuid())
                                        .then(argument("user", UserdataByOnlineUUIDArgumentType.user())
                                                .executes(this::executeModifyRedirectUuid)
                                        )
                                )
                        )
                );
    }

    private int executeInfo(CommandContext<ISender> context) {
        User user = UserdataByOnlineUUIDArgumentType.getUser(context, "user");
        user.setService(getCore().getYggdrasilServicesHandler().getYggdrasilService(user.getYggdrasilService()));

        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_info", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build(),
                FormatContent.FormatEntry.builder().name("redirect_uuid").content(user.getRedirectUuid().toString()).build(),
                FormatContent.FormatEntry.builder().name("yggdrasil_name").content(user.getService() == null ? "unknown" : user.getService().getName()).build(),
                FormatContent.FormatEntry.builder().name("yggdrasil_path").content(user.getYggdrasilService()).build(),
                FormatContent.FormatEntry.builder().name("whitelist").content(user.isWhitelist()).build()
        )));
        return 0;
    }

    private int executeModifyRedirectUuid(CommandContext<ISender> context) {
        UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        User user = UserdataByOnlineUUIDArgumentType.getUser(context, "user");
        UUID old_redirect_uuid = user.getRedirectUuid();

        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("confirm").content(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_modify_redirect_uuid_confirm", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                        FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build(),
                        FormatContent.FormatEntry.builder().name("old_redirect_uuid").content(old_redirect_uuid.toString()).build(),
                        FormatContent.FormatEntry.builder().name("new_redirect_uuid").content(user.getRedirectUuid().toString()).build()
                ))).build()
        )));


        getSecondaryConfirmationHandler().submit(context.getSource(), value -> {

            user.setRedirectUuid(uuid);
            getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_modify_redirect_uuid_complete", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                    FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build(),
                    FormatContent.FormatEntry.builder().name("old_redirect_uuid").content(old_redirect_uuid.toString()).build(),
                    FormatContent.FormatEntry.builder().name("new_redirect_uuid").content(user.getRedirectUuid().toString()).build()
            )));
        });
        return 0;
    }

    private int executeModifyYggdrasil(CommandContext<ISender> context) {
        YggdrasilService service = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        User user = UserdataByOnlineUUIDArgumentType.getUser(context, "user");
        String old_yggdrasil = user.getYggdrasilService();

        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("confirm").content(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_modify_yggdrasil_confirm", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                        FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build(),
                        FormatContent.FormatEntry.builder().name("old_yggdrasil").content(old_yggdrasil).build(),
                        FormatContent.FormatEntry.builder().name("new_yggdrasil_name").content(service.getName()).build(),
                        FormatContent.FormatEntry.builder().name("new_yggdrasil_path").content(service.getPath()).build()
                ))).build()
        )));


        getSecondaryConfirmationHandler().submit(context.getSource(), value -> {
            user.setYggdrasilService(service.getPath());
            getCore().getSqlManager().getUserDataHandler().updateUserEntry(user);
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_modify_yggdrasil_complete", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                    FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build(),
                    FormatContent.FormatEntry.builder().name("old_yggdrasil").content(old_yggdrasil).build(),
                    FormatContent.FormatEntry.builder().name("new_yggdrasil_name").content(service.getName()).build(),
                    FormatContent.FormatEntry.builder().name("new_yggdrasil_path").content(service.getPath()).build()
            )));
        });
        return 0;
    }

    private int executeRemove(CommandContext<ISender> context) {
        User user = UserdataByOnlineUUIDArgumentType.getUser(context, "user");

        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("confirm").content(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_remove_confirm", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                        FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build()
                ))).build()
        )));

        getSecondaryConfirmationHandler().submit(context.getSource(), value -> {
            if (getCore().getSqlManager().getUserDataHandler().deleteUserEntry(user)) {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_removed", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                        FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build()
                )));
            } else {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_remove_repeat", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("name").content(user.getCurrentName()).build(),
                        FormatContent.FormatEntry.builder().name("online_uuid").content(user.getOnlineUuid().toString()).build()
                )));
            }
        });
        return 0;
    }
}
