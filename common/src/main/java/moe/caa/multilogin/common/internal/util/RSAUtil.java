package moe.caa.multilogin.common.internal.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {

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

    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keySize);
        return gen.generateKeyPair();
    }

    public static PrivateKey loadPrivateKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        if (isPEMFormat(keyBytes)) {
            String pem = new String(keyBytes);
            keyBytes = parsePEMContent(pem, "PRIVATE KEY");
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        if (isPEMFormat(keyBytes)) {
            String pem = new String(keyBytes);
            keyBytes = parsePEMContent(pem, "PUBLIC KEY");
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static boolean isPEMFormat(byte[] data) {
        String s = new String(data);
        return s.contains("BEGIN") && s.contains("END");
    }

    private static byte[] parsePEMContent(String pem, String type) {
        String normalized = pem
                .replaceAll("-----BEGIN " + type + "-----", "")
                .replaceAll("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    public static void savePublicKey(PublicKey publicKey, Path path) throws IOException {
        byte[] encoded = publicKey.getEncoded();
        savePEMFile("PUBLIC KEY", encoded, path);
    }

    public static void savePrivateKey(PrivateKey privateKey, Path path) throws IOException {
        byte[] encoded = privateKey.getEncoded();
        savePEMFile("PRIVATE KEY", encoded, path);
    }

    private static void savePEMFile(String type, byte[] data, Path path) throws IOException {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(data);
        String pem = "-----BEGIN " + type + "-----\n"
                + base64
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem, StandardCharsets.UTF_8);
    }
}
