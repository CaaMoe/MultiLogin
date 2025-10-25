package moe.caa.multilogin.common.internal.config;

import org.spongepowered.configurate.NodePath;

public final class RemoteAuthenticationConfig extends AuthenticationConfig {
    public final ConfigurationValue<String> rsaPublicKeyPath;
    public final ConfigurationValue<String> verifyDigitalSignatureAlgorithm;

    public RemoteAuthenticationConfig(MainConfig mainConfig) {
        super(mainConfig);

        this.rsaPublicKeyPath = string(NodePath.path("rsa-public-key-path"));
        this.verifyDigitalSignatureAlgorithm = string(NodePath.path("verify-digital-signature-algorithm"));
    }
}