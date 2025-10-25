package moe.caa.multilogin.common.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class InfoCommand<S> extends SubCommand<S> {
    public InfoCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionMe = "multilogin.command.me";
        String permissionInfo = "multilogin.command.info";
        addCommandDescription("me", permissionMe, manager.core.messageConfig.commandDescriptionMe.get());
        addCommandDescription("info <target>", permissionInfo, manager.core.messageConfig.commandDescriptionInfo.get());

        builder.then(literal("me")
                .requires(predicateHasPermission(permissionMe))
                .executes(context -> {
                    manager.executeAsync(() -> me(context.getSource()));
                    return Command.SINGLE_SUCCESS;
                })
        );

        builder.then(literal("info")
                .then(argument("target", StringArgumentType.string())
                        .requires(predicateHasPermission(permissionInfo))
                        .executes(context -> {
                            manager.executeAsync(() -> infoOther(context.getSource(), StringArgumentType.getString(context, "target")));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private void infoOther(S source, String target) {
        Sender sender = manager.wrapSender(source);
        resolveOnlinePlayerRunOrElseTip(source, target, targetPlayer -> {
            ifFetchOtherOnlineDataRunOrElseTip(sender, targetPlayer, data -> {
                sender.sendMessage(replacePlaceholder(data, manager.core.messageConfig.commandInfoContent.get().build()));
            });
        });
    }

    protected void me(S s) {
        Sender sender = manager.wrapSender(s);
        ifOnlinePlayerRunOrElseTip(sender, player -> {
            OnlineData data = player.getOnlineData();
            if (data == null) {
                player.sendMessage(manager.core.messageConfig.commandGeneralNotFoundOnlineDataMe.get().build());
            } else {
                player.sendMessage(replacePlaceholder(data, manager.core.messageConfig.commandMeContent.get().build()));
            }
        });
    }

    private Component replacePlaceholder(OnlineData data, Component component) {
        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<user_name>")
                .replacement(data.onlineUser().authenticatedGameProfile().username())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<user_uuid>")
                .replacement(data.onlineUser().authenticatedGameProfile().uuid().toString())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<service_display_name>")
                .replacement(data.onlineUser().service().displayName.get())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<profile_name>")
                .replacement(data.onlineProfile().profileName())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<profile_uuid>")
                .replacement(data.onlineProfile().profileUUID().toString())
                .build());

        return component;
    }
}
