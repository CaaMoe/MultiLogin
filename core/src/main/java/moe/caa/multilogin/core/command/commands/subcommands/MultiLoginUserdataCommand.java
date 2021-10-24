package moe.caa.multilogin.core.command.commands.subcommands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.arguments.UUIDArgumentType;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.UUID;

public class MultiLoginUserdataCommand extends BaseSubCommand{
    public MultiLoginUserdataCommand(MultiCore core) {
        super(core);
    }

    @Override
    public ArgumentBuilder<ISender, ?> getSubExecutor() {
        return literal("userdata")
                .then(literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_USERDATA_REMOVE))
                        .then(argument("uuid", UUIDArgumentType.uuid())
                                .executes(this::executeRemove)
                        )
                );
    }

    private int executeRemove(CommandContext<ISender> context) {
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("confirm").content(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_remove_confirm", FormatContent.empty())).build()
        )));

        UUID target = UUIDArgumentType.getUuid(context, "uuid");
        getSecondaryConfirmationHandler().submit(context.getSource(), value -> {
            if (getCore().getSqlManager().getUserDataHandler().deleteUserEntry(target)) {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_removed", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("uuid").content(target.toString()).build()
                )));
            } else {
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_userdata_remove_repeat", FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("uuid").content(target.toString()).build()
                )));
            }
        });
        return 0;
    }
}
