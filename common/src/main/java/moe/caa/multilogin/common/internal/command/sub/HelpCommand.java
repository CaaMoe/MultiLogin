package moe.caa.multilogin.common.internal.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

public class HelpCommand<SENDER> extends SubCommand<SENDER> {
    private final String permission = "multilogin.command.help";

    public HelpCommand(CommandManager<SENDER> manager) {
        super(manager);

    }

    @Override
    public void register(ArgumentBuilder<SENDER, ?> builder) {
        addCommandDescription("help", permission, manager.core.messageConfig.commandDescriptionHelp.get());
        builder.then(literal("help")
                .requires(predicateHasPermission(permission))
                .executes(context -> {
                    List<CommandDescription> descriptions = manager.subCommands.stream()
                            .flatMap(it -> it.commandDescriptions.stream())
                            .filter(it -> hasPermission(context.getSource(), it.permission())).toList();

                    sendMessage(context.getSource(), manager.core.messageConfig.commandHelpHeader.get());
                    for (CommandDescription description : descriptions) {
                        String command = "/multilogin " + description.label();
                        sendMessage(context.getSource(), manager.core.messageConfig.commandHelpEntry.get()
                                .replaceText(TextReplacementConfig.builder()
                                        .matchLiteral("<description>")
                                        .replacement(description.description())
                                        .build())
                                .replaceText(TextReplacementConfig.builder()
                                        .matchLiteral("<command>")
                                        .replacement(Component.text(command))
                                        .build()
                                ).clickEvent(ClickEvent.suggestCommand(command))
                        );
                    }

                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
