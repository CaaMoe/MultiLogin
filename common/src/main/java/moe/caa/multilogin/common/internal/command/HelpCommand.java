package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

public class HelpCommand<S> extends SubCommand<S> {
    public HelpCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permission = "multilogin.command.help";
        addCommandDescription("help", permission, manager.core.messageConfig.commandDescriptionHelp.get());
        builder.then(literal("help")
                .requires(predicateHasPermission(permission))
                .executes(context -> {
                    return manager.executeAsync(context, () -> showHelp(context.getSource()));
                })
        );
    }

    public void showHelp(S s) {
        Sender sender = manager.wrapSender(s);

        List<CommandDescription> descriptions = manager.subCommands.stream()
                .flatMap(it -> it.commandDescriptions.stream())
                .filter(it -> hasPermission(s, it.permission())).toList();

        if (descriptions.isEmpty()) {
            sender.sendMessage(manager.core.messageConfig.commandHelpNone.get().build());
        }

        sender.sendMessage(manager.core.messageConfig.commandHelpHeader.get().build());
        for (CommandDescription description : descriptions) {
            String command = "/multilogin " + description.label();
            sender.sendMessage(manager.core.messageConfig.commandHelpEntry.get()
                    .replace("<description>", description.description().originalMiniMessageStr())
                    .replace("<command>", command)
                    .replace("<permission>", description.permission())
                    .build()
                    .clickEvent(ClickEvent.suggestCommand(command))
            );
        }
    }
}
