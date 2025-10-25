package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.common.internal.util.RSAUtil;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public record SignedCookieData(CookieData cookieData, String base64Signature, String base64SignContent) {

    public static SignedCookieData readSignedCookieData(byte[] bytes) throws Throwable {
        JsonObject parsedJsonObject = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        String base64Signature = parsedJsonObject.getAsJsonPrimitive("signature").getAsString();
        String base64SignContent = parsedJsonObject.getAsJsonObject("content").getAsString();

        CookieData data = CookieData.deserialize(JsonParser.parseString(
                new String(Base64.getDecoder().decode(base64SignContent), StandardCharsets.UTF_8)
        ).getAsJsonObject());
        return new SignedCookieData(data, base64Signature, base64SignContent);
    }

    public static SignedCookieData signData(CookieData cookieData, PrivateKey key, String algorithm) throws Exception {
        JsonObject data = new JsonObject();
        cookieData.deserializeData(data);
        String base64signContent = Base64.getEncoder().encodeToString(data.toString().getBytes(StandardCharsets.UTF_8));
        String base64Signature = Base64.getEncoder().encodeToString(RSAUtil.sign(base64signContent.getBytes(StandardCharsets.UTF_8), key, algorithm));
        return new SignedCookieData(cookieData, base64Signature, base64signContent);
    }

    public boolean validateSignature(PublicKey publicKey, String algorithm) throws Exception {
        return RSAUtil.verify(
                base64SignContent.getBytes(StandardCharsets.UTF_8),
                Base64.getDecoder().decode(base64Signature),
                publicKey, algorithm
        );
    }

    public byte[] toBytes() {
        JsonObject data = new JsonObject();
        data.addProperty("signature", base64Signature);
        data.addProperty("content", base64SignContent);
        return data.toString().getBytes(StandardCharsets.UTF_8);
    }
}
