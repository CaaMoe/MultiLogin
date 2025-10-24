package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.util.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.configurate.NodePath;

public sealed abstract class AuthenticationConfig extends Configuration permits LocalAuthenticationConfig, RemoteAuthenticationConfig {
    public final ConfigurationValue<String> id;
    public final ConfigurationValue<Component> displayName;
    public final ConfigurationValue<MainConfig.UUIDInitPolicy> uuidInitPolicy;
    public final ConfigurationValue<String> nameInitFormat;
    public final ConfigurationValue<ProfileManager.UUIDConflictPolicy> uuidConflictPolicy;
    public final ConfigurationValue<ProfileManager.NameConflictPolicy> nameConflictPolicy;
    public final ConfigurationValue<Boolean> whitelist;
    protected final MainConfig mainConfig;

    public AuthenticationConfig(MainConfig mainConfig) {
        this.mainConfig = mainConfig;
        this.id = string(NodePath.path("id"));
        this.displayName = miniMsgOpt(NodePath.path("display-name"), Component.text("Unnamed Authentication").color(TextColor.color(0, 255, 0)));
        this.uuidInitPolicy = enumConstantOpt(NodePath.path("uuid-init-policy"), MainConfig.UUIDInitPolicy.class, mainConfig.defaultUuidInitPolicy.get());
        this.nameInitFormat = stringOpt(NodePath.path("name-init-format"), mainConfig.defaultNameInitFormat.get());
        this.uuidConflictPolicy = enumConstantOpt(NodePath.path("uuid-conflict-policy"), ProfileManager.UUIDConflictPolicy.class, mainConfig.defaultUuidConflictPolicy.get());
        this.nameConflictPolicy = enumConstantOpt(NodePath.path("name-conflict-policy"), ProfileManager.NameConflictPolicy.class, mainConfig.defaultNameConflictPolicy.get());
        this.whitelist = boolOpt(NodePath.path("whitelist"), mainConfig.defaultWhitelist.get());
    }
}
