package moe.caa.multilogin.common.internal.config.authentication;

import moe.caa.multilogin.common.internal.config.MainConfig;
import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class AuthenticationOptionConfig extends Configuration {
    public final ConfigurationValue<MainConfig.UUIDInitPolicy> uuidInitPolicy;
    public final ConfigurationValue<ProfileManager.UUIDConflictPolicy> uuidConflictPolicy;
    public final ConfigurationValue<String> nameInitFormat;
    public final ConfigurationValue<ProfileManager.NameConflictPolicy> nameConflictPolicy;
    public final ConfigurationValue<Boolean> whitelist;

    public AuthenticationOptionConfig() {
        this.uuidInitPolicy = enumConstantOpt(NodePath.path("uuid-init-policy"), MainConfig.UUIDInitPolicy.class, MainConfig.UUIDInitPolicy.INHERIT);
        this.uuidConflictPolicy = enumConstantOpt(NodePath.path("uuid-conflict-policy"), ProfileManager.UUIDConflictPolicy.class, ProfileManager.UUIDConflictPolicy.RANDOM);
        this.nameInitFormat = stringOpt(NodePath.path("name-init-format"), "%name%");
        this.nameConflictPolicy = enumConstantOpt(NodePath.path("name-conflict-policy"), ProfileManager.NameConflictPolicy.class, ProfileManager.NameConflictPolicy.INCREMENT_RIGHT_TRUNCATE);
        this.whitelist = boolOpt(NodePath.path("whitelist"), false);
    }

    public AuthenticationOptionConfig(AuthenticationOptionConfig defaultOption) {
        this.uuidInitPolicy = enumConstantOpt(NodePath.path("uuid-init-policy"), MainConfig.UUIDInitPolicy.class, defaultOption.uuidInitPolicy.get());
        this.uuidConflictPolicy = enumConstantOpt(NodePath.path("uuid-conflict-policy"), ProfileManager.UUIDConflictPolicy.class, defaultOption.uuidConflictPolicy.get());
        this.nameInitFormat = stringOpt(NodePath.path("name-init-format"), defaultOption.nameInitFormat.get());
        this.nameConflictPolicy = enumConstantOpt(NodePath.path("name-conflict-policy"), ProfileManager.NameConflictPolicy.class, defaultOption.nameConflictPolicy.get());
        this.whitelist = boolOpt(NodePath.path("whitelist"), defaultOption.whitelist.get());
    }
}
