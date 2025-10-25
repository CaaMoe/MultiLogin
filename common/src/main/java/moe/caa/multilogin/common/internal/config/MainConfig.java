package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.config.authentication.AuthenticationOptionConfig;
import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfigPath = stringOpt(NodePath.path("database-config-path"), "hikari.properties");
    public final ConfigurationValue<AuthMode> authMode = enumConstantOpt(NodePath.path("auth-mode"), AuthMode.class, AuthMode.LOCAL);
    public final ConfigurationValue<AuthenticationOptionConfig> defaultAuthServiceOption = sub(NodePath.path("default-auth-service-option"), new AuthenticationOptionConfig());

    public final ConfigurationValue<Boolean> disableHelloPacketUsernameValidation = boolOpt(NodePath.path("disable-hello-packet-username-validation"), false);
    public final ConfigurationValue<Boolean> reconnectFeatureEnable = boolOpt(NodePath.path("reconnect-feature-enable"), true);


    public final ConfigurationValue<LocalRSAConfig> localRsa = sub(NodePath.path("local-rsa"), new LocalRSAConfig());


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
