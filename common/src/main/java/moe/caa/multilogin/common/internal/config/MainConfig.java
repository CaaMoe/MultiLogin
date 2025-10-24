package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfigPath = stringOpt(NodePath.path("database-config-path"), "hikari.properties");
    public final ConfigurationValue<AuthMode> authMode = enumConstantOpt(NodePath.path("auth-mode"), AuthMode.class, AuthMode.LOCAL);
    public final ConfigurationValue<Boolean> defaultAlwaysEnableEncryptedConnection = boolOpt(NodePath.path("global-always-enable-encrypted-connection"), true);
    public final ConfigurationValue<UUIDInitPolicy> defaultUuidInitPolicy = enumConstantOpt(NodePath.path("global-uuid-init-policy"), UUIDInitPolicy.class, UUIDInitPolicy.INHERIT);
    public final ConfigurationValue<String> defaultNameInitFormat = stringOpt(NodePath.path("global-name-init-format"), "%name%");
    public final ConfigurationValue<ProfileManager.UUIDConflictPolicy> defaultUuidConflictPolicy = enumConstantOpt(NodePath.path("global-uuid-conflict-policy"), ProfileManager.UUIDConflictPolicy.class, ProfileManager.UUIDConflictPolicy.RANDOM);
    public final ConfigurationValue<ProfileManager.NameConflictPolicy> defaultNameConflictPolicy = enumConstantOpt(NodePath.path("global-name-conflict-policy"), ProfileManager.NameConflictPolicy.class, ProfileManager.NameConflictPolicy.INCREMENT_RIGHT_TRUNCATE);
    public final ConfigurationValue<Boolean> defaultWhitelist = boolOpt(NodePath.path("global-whitelist"), false);

    public enum AuthMode {
        LOCAL,
        MIXED,
        REMOTE;
    }

    public enum UUIDInitPolicy {
        RANDOM,
        OFFLINE,
        INHERIT;
    }
}
