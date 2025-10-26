package moe.caa.multilogin.common.internal.command.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.command.SubCommand;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.manager.CommandManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ReloadCommand<S> extends SubCommand<S> {
    public ReloadCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionReload = "multilogin.command.admin.reload";

        addCommandDescription("admin reload", permissionReload, manager.core.messageConfig.commandDescriptionAdminReload.get());
        builder.then(literal("admin")
                .then(literal("reload")
                        .requires(predicateHasPermission(permissionReload))
                        .executes(context -> {
                            manager.executeAsync(context, () -> reload(context.getSource()));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private void reload(S s) throws IOException, NoSuchAlgorithmException {
        Sender sender = manager.wrapSender(s);
        manager.core.reload();
        sender.sendMessage(manager.core.messageConfig.commandAdminReloadSucceed.get().build());
    }
}
