package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.NodePath;

public class MessageConfig extends Configuration {
    public final ConfigurationValue<Component> loginUnknownError = miniMsg(NodePath.path("login-unknown-error"));
    public final ConfigurationValue<Component> loginInvalidCharactersInName = miniMsg(NodePath.path("login-invalid-characters-in-name"));

    public final ConfigurationValue<Component> commandDescriptionHelp = miniMsg(NodePath.path("command-description-help"));
    public final ConfigurationValue<Component> commandHelpHeader = miniMsg(NodePath.path("command-help-header"));
    public final ConfigurationValue<Component> commandHelpEntry = miniMsg(NodePath.path("command-help-entry"));
    public final ConfigurationValue<Component> commandHelpFooter = miniMsg(NodePath.path("command-help-footer"));

    private ConfigurationValue<Component> miniMsg(NodePath path) {
        return raw(path, node -> MiniMessage.miniMessage().deserialize(node.getString("")));
    }
}
