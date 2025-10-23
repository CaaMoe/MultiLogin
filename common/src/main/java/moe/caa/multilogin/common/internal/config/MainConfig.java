package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.util.Configuration;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfig = stringOpt(NodePath.path("database-config"), "hikari.properties");
    public final ConfigurationValue<AuthMode> authMode = enumConstantOpt(NodePath.path("auth-mode"), AuthMode.class, AuthMode.LOCAL);


    public enum AuthMode {
        LOCAL,
        REMOTE;
    }

    public enum UUIDInitPolicy {
        RANDOM,
        OFFLINE,
        INHERIT;
    }

    public static sealed abstract class AuthenticationConfig extends Configuration {
        public final ConfigurationValue<String> id = string(NodePath.path("id"));
        public final ConfigurationValue<Component> displayName = miniMsg(NodePath.path("display-name"));
        public final ConfigurationValue<UUIDInitPolicy> uuidInitPolicy = enumConstantOpt(NodePath.path("uuid-init-policy"), UUIDInitPolicy.class, UUIDInitPolicy.INHERIT);
        public final ConfigurationValue<String> nameInitFormat = stringOpt(NodePath.path("name-init-format"), "%name%");
        public final ConfigurationValue<ProfileManager.UUIDConflictPolicy> uuidConflictPolicy = enumConstantOpt(NodePath.path("uuid-conflict-policy"), ProfileManager.UUIDConflictPolicy.class, ProfileManager.UUIDConflictPolicy.RANDOM);
        public final ConfigurationValue<ProfileManager.NameConflictPolicy> nameConflictPolicy = enumConstantOpt(NodePath.path("name-conflict-policy"), ProfileManager.NameConflictPolicy.class, ProfileManager.NameConflictPolicy.INCREMENT_RIGHT_TRUNCATE);
    }

    public static final class LocalAuthenticationConfig extends AuthenticationConfig {
    }

    public static final class RemoteAuthenticationConfig extends AuthenticationConfig {
        public final ConfigurationValue<String> publicKeyPath = string(NodePath.path("public-key-path"));
    }
}
