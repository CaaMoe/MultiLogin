package moe.caa.multilogin.common.internal.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import moe.caa.multilogin.common.internal.command.CommandManager;
import moe.caa.multilogin.common.internal.command.Sender;
import moe.caa.multilogin.common.internal.online.OnlineData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class InfoCommand<S> extends SubCommand<S> {
    public InfoCommand(CommandManager<S> manager) {
        super(manager);
    }

    @Override
    public void register(ArgumentBuilder<S, ?> builder) {
        String permissionSelf = "multilogin.command.info";
        String permissionOther = "multilogin.command.info.other";
        addCommandDescription("info", permissionSelf, manager.core.messageConfig.commandDescriptionInfo.get());
        addCommandDescription("info <target>", permissionOther, manager.core.messageConfig.commandDescriptionInfoOther.get());

        builder.then(literal("info")
                .requires(predicateHasPermission(permissionSelf))
                .executes(context -> {
                    manager.executeAsync(() -> infoSelf(context.getSource()));
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("target", StringArgumentType.string())
                        .requires(predicateHasPermission(permissionOther))
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
            OnlineData data = targetPlayer.getOnlineData();
            if (data == null) {
                sender.sendMessage(manager.core.messageConfig.commandInfoOtherNone.get());
            } else {
                sender.sendMessage(replacePlaceholder(data, manager.core.messageConfig.commandInfoOtherContent.get()));
            }
        });
    }

    protected void infoSelf(S s) {
        ifOnlinePlayerRunOrElseTip(s, player -> {
            OnlineData data = player.getOnlineData();
            if (data == null) {
                player.sendMessage(manager.core.messageConfig.commandInfoNone.get());
            } else {
                player.sendMessage(replacePlaceholder(data, manager.core.messageConfig.commandInfoContent.get()));
            }
        });
    }

    private Component replacePlaceholder(OnlineData data, Component component) {
        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<user_name>")
                .replacement(data.onlineUser.username())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<user_uuid>")
                .replacement(data.onlineUser.userUUID().toString())
                .build());

        // todo <service_display_name>

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<profile_name>")
                .replacement(data.onlineProfile.profileName())
                .build());

        component = component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("<profile_uuid>")
                .replacement(data.onlineProfile.profileUUID().toString())
                .build());

        return component;
    }
}
