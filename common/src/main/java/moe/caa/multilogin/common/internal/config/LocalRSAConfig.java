package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

import java.security.PrivateKey;
import java.security.PublicKey;

public class LocalRSAConfig extends Configuration {
    public final ConfigurationValue<PublicKey> publicKey;
    public final ConfigurationValue<PrivateKey> privateKey;
    public final ConfigurationValue<String> verifyDigitalSignatureAlgorithm;


    public LocalRSAConfig() {
        this.publicKey = rsaPublicKey(NodePath.path("public-key-path"));
        this.privateKey = rsaPrivateKey(NodePath.path("private-key-path"));
        this.verifyDigitalSignatureAlgorithm = stringOpt(NodePath.path("verify-digital-signature-algorithm"), "SHA512withRSA");
    }
}
