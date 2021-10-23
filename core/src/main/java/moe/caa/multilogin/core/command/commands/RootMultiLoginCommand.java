package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;

public class RootMultiLoginCommand extends BaseCommand {
    public RootMultiLoginCommand(MultiCore core) {
        super(core);
    }

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                literal("multilogin")
                        .then(literal("reload")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                                .executes(this::executeReload)
                        )
        );
    }

    private int executeReload(CommandContext<ISender> context) {
        getCore().reload();
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_reloaded", FormatContent.empty()));
        return 0;
    }
}
