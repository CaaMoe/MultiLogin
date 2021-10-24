package moe.caa.multilogin.core.command.commands.subcommands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.arguments.UUIDArgumentType;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.UUID;

public class MultiLoginSkinRestorerCommand extends BaseSubCommand {
    public MultiLoginSkinRestorerCommand(MultiCore core) {
        super(core);
    }

    @Override
    public ArgumentBuilder<ISender, ?> getSubExecutor() {
        return literal("skinrestorer")
                .then(literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_SKIN_RESTORER_REMOVE))
                        .then(argument("uuid", UUIDArgumentType.uuid())
                                .executes(this::executeRemove)
                        )
                );
    }

    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        if (getCore().getSqlManager().getSkinRestorerDataHandler().deleteRestorerEntry(uuid)) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_skin_restorer_removed", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("uuid").content(uuid.toString()).build()
            )));
        } else {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_multilogin_skin_restorer_remove_repeat", FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("uuid").content(uuid.toString()).build()
            )));
        }
        return 0;
    }
}
