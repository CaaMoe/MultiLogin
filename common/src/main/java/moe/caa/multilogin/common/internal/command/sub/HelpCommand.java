package moe.caa.multilogin.common.internal.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

public class HelpCommand<SENDER> extends SubCommand<SENDER> {
    public HelpCommand(CommandManager<SENDER> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<SENDER, ?> builder) {
        String permission = "multilogin.command.help";
        addCommandDescription("help", permission, manager.core.messageConfig.commandDescriptionHelp.get());
        builder.then(literal("help")
                .requires(predicateHasPermission(permission))
                .executes(context -> {
                    showHelp(context.getSource());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    public void showHelp(SENDER sender) {
        List<CommandDescription> descriptions = manager.subCommands.stream()
                .flatMap(it -> it.commandDescriptions.stream())
                .filter(it -> hasPermission(sender, it.permission())).toList();

        if (descriptions.isEmpty()) {
            sendMessage(sender, manager.core.messageConfig.commandHelpNone.get());
        }

        sendMessage(sender, manager.core.messageConfig.commandHelpHeader.get());
        for (CommandDescription description : descriptions) {
            String command = "/multilogin " + description.label();
            sendMessage(sender, manager.core.messageConfig.commandHelpEntry.get()
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<description>")
                            .replacement(description.description())
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<command>")
                            .replacement(Component.text(command))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<permission>")
                            .replacement(Component.text(description.permission()))
                            .build())
                    .clickEvent(ClickEvent.suggestCommand(command))
            );
        }
    }
}
