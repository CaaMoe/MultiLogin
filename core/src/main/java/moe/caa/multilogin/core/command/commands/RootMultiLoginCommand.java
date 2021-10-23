package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.main.Version;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Optional;

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
                        .then(literal("update")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_UPDATE))
                                .executes(this::executeUpdate)
                        )
                        .then(literal("confirm")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_CONFIRM))
                                .executes(this::executeConfirm)
                        )
        );
    }

    @SneakyThrows
    private int executeConfirm(CommandContext<ISender> context) {
        if (!getSecondaryConfirmationHandler().confirm(context.getSource())) {
            context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_secondary_confirmation_unknown", FormatContent.empty()));
        }
        return 0;
    }

    private int executeUpdate(CommandContext<ISender> context) {
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_start", FormatContent.empty()));
        getCore().getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                if (getCore().getUpdater().shouldUpdate()) {
                    context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_need", FormatContent.createContent(
                            FormatContent.FormatEntry.builder().name("latest").content(getCore().getUpdater().latestVersion).build(),
                            FormatContent.FormatEntry.builder().name("current").content(Optional.ofNullable(getCore().getUpdater().currentVersion).map(Version::toString).orElse(getCore().getPlugin().getPluginVersion())).build()
                    )));
                } else {
                    context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_unwanted", FormatContent.empty()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_update_failed", FormatContent.empty()));
            }
        });
        return 0;
    }

    private int executeReload(CommandContext<ISender> context) {
        getCore().reload();
        context.getSource().sendMessage(getCore().getLanguageHandler().getMessage("command_message_reloaded", FormatContent.empty()));
        return 0;
    }
}
