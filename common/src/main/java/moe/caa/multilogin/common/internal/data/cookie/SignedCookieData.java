package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.common.internal.util.RSASignatureUtil;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public record SignedCookieData(CookieData cookieData, String base64Signature, String signContent) {

    public static SignedCookieData readSignedCookieData(byte[] bytes) throws Throwable {
        JsonObject parsedJsonObject = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        String base64Signature = parsedJsonObject.getAsJsonPrimitive("signature").getAsString();
        String signContent = parsedJsonObject.getAsJsonObject("sign_data").getAsString();

        CookieData data = CookieData.deserialize(JsonParser.parseString(signContent).getAsJsonObject());
        return new SignedCookieData(data, base64Signature, signContent);
    }

    public static SignedCookieData signData(CookieData cookieData, PrivateKey key, String algorithm) throws Exception {
        JsonObject data = new JsonObject();
        cookieData.deserializeData(data);
        String signContent = data.toString();
        String base64Signature = Base64.getEncoder()
                .encodeToString(RSASignatureUtil.sign(signContent.getBytes(StandardCharsets.UTF_8), key, algorithm));
        return new SignedCookieData(cookieData, base64Signature, signContent);
    }

    public boolean validateSignature(PublicKey publicKey, String algorithm) throws Exception {
        return RSASignatureUtil.verify(
                signContent.getBytes(StandardCharsets.UTF_8),
                Base64.getDecoder().decode(base64Signature),
                publicKey, algorithm
        );
    }
}
