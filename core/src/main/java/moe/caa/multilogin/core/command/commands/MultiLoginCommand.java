package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;

public class MultiLoginCommand {
    private final CommandHandler handler;

    public MultiLoginCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public void register() {
        handler.getDispatcher().register(
                handler.literal("multilogin")
                        .then(handler.literal("reload")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                                .executes(this::executeReload)
                        ).then(handler.literal("")

                        )
        );

    }

    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        handler.getCore().getPluginConfig().reload();
        context.getSource().sendMessage(handler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
        return 0;
    }
}
