package moe.caa.multilogin.common.internal.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class RSASignatureUtil {

    public static byte[] sign(byte[] data, PrivateKey privateKey, String algorithm) throws Exception {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean verify(byte[] data, byte[] signatureByte, PublicKey publicKey, String algorithm) throws Exception {
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureByte);
    }
}
