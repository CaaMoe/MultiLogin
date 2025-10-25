package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
                    manager.executeAsync(() -> showHelp(context.getSource()));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    public void showHelp(S s) {
        List<CommandDescription> descriptions = manager.subCommands.stream()
                .flatMap(it -> it.commandDescriptions.stream())
                .filter(it -> hasPermission(s, it.permission())).toList();

        if (descriptions.isEmpty()) {
            sendMessage(s, manager.core.messageConfig.commandHelpNone.get().build());
        }

        sendMessage(s, manager.core.messageConfig.commandHelpHeader.get().build());
        for (CommandDescription description : descriptions) {
            String command = "/multilogin " + description.label();
            sendMessage(s, manager.core.messageConfig.commandHelpEntry.get()
                    .replace("<description>", description.description().originalMiniMessageStr())
                    .replace("<command>", command)
                    .replace("<permission>", description.permission())
                    .build()
                    .clickEvent(ClickEvent.suggestCommand(command))
            );
        }
    }
}
