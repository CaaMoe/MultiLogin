package moe.caa.multilogin.common.internal.config.authentication;

import moe.caa.multilogin.common.internal.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.configurate.NodePath;

public sealed abstract class AuthenticationConfig extends AuthenticationOptionConfig permits LocalAuthenticationConfig, RemoteAuthenticationConfig {
    public final ConfigurationValue<String> id;
    public final ConfigurationValue<Component> displayName;
    protected final MainConfig mainConfig;

    public AuthenticationConfig(MainConfig mainConfig) {
        super(mainConfig.defaultAuthServiceOption.get());
        this.mainConfig = mainConfig;
        this.id = string(NodePath.path("id"));
        this.displayName = miniMsgOpt(NodePath.path("display-name"), Component.text("Unnamed Authentication").color(TextColor.color(0, 255, 0)));
    }
}
