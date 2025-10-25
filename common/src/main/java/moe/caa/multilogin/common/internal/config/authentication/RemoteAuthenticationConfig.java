package moe.caa.multilogin.common.internal.config.authentication;

import moe.caa.multilogin.common.internal.config.MainConfig;
import org.spongepowered.configurate.NodePath;

import java.security.PublicKey;

public final class RemoteAuthenticationConfig extends AuthenticationConfig {
    public final ConfigurationValue<PublicKey> remoteRSAPublicKey;
    public final ConfigurationValue<String> remoteRSAVerifyDigitalSignatureAlgorithm;

    public RemoteAuthenticationConfig(MainConfig mainConfig) {
        super(mainConfig);

        this.remoteRSAPublicKey = rsaPublicKey(NodePath.path("remote-rsa-public-key-path"));
        this.remoteRSAVerifyDigitalSignatureAlgorithm = string(NodePath.path("remote-rsa-verify-digital-signature-algorithm"));
    }
}