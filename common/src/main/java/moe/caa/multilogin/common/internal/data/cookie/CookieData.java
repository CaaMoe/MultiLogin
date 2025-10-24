package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.common.internal.util.RSASignatureUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public sealed abstract class CookieData permits ExpirableData {
    private static Map<String, MethodHandle> typeCookieMap = Collections.emptyMap();
    private ReadSignatureContent readSignatureContent;

    public static void init() throws IllegalAccessException, NoSuchMethodException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Map<String, MethodHandle> cookieTypeMap = new HashMap<>();
        for (Class<?> permittedSubclass : CookieData.class.getPermittedSubclasses()) {
            cookieTypeMap.putAll(collectTypeMap(lookup, permittedSubclass));
        }
        CookieData.typeCookieMap = Collections.unmodifiableMap(cookieTypeMap);
    }

    private static Map<String, MethodHandle> collectTypeMap(MethodHandles.Lookup lookup, Class<?> sealedClass) throws IllegalAccessException, NoSuchMethodException {
        Map<String, MethodHandle> cookieTypeMap = new HashMap<>();
        for (Class<?> permittedSubclass : sealedClass.getPermittedSubclasses()) {
            if (permittedSubclass.isSealed()) {
                cookieTypeMap.putAll(collectTypeMap(lookup, permittedSubclass));
            } else {
                if (!permittedSubclass.isAnnotationPresent(CookieDataType.class)) {
                    throw new IllegalStateException(permittedSubclass.getCanonicalName() + " is not annotated with " + CookieDataType.class.getCanonicalName());
                }

                cookieTypeMap.put(permittedSubclass.getAnnotation(CookieDataType.class).type(), lookup.unreflectConstructor(permittedSubclass.getConstructor()));
            }
        }
        return cookieTypeMap;
    }

    public static byte[] serializeNoSignature(CookieData cookieData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", cookieData.getClass().getAnnotation(CookieDataType.class).type());
        cookieData.serializeData(jsonObject);
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] serializeSignature(CookieData cookieData, PrivateKey key, String algorithm) throws Exception {
        JsonObject originalData = new JsonObject();
        originalData.addProperty("type", cookieData.getClass().getAnnotation(CookieDataType.class).type());
        cookieData.serializeData(originalData);

        String signContent = originalData.toString();
        String base64Signature = Base64.getEncoder().encodeToString(RSASignatureUtil.sign(signContent.getBytes(StandardCharsets.UTF_8), key, algorithm));

        JsonObject signatureJsonObject = new JsonObject();
        signatureJsonObject.add("sign_data", originalData);
        signatureJsonObject.addProperty("signature", base64Signature);

        return signatureJsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static CookieData deserialize(byte[] data) throws Throwable {
        JsonObject jsonObject = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();

        ReadSignatureContent readSignatureContent = null;
        if (jsonObject.has("signature")) {
            jsonObject = jsonObject.getAsJsonObject("sign_data");
            String signData = jsonObject.toString();
            String signature = jsonObject.getAsJsonPrimitive("signature").getAsString().trim();
            readSignatureContent = new ReadSignatureContent(signData, signature);
        }
        String type = jsonObject.getAsJsonPrimitive("type").getAsString();
        MethodHandle handle = typeCookieMap.get(type);
        if (handle == null) {
            throw new IllegalArgumentException("Invalid data type: " + type);
        }
        CookieData cookieData = (CookieData) handle.invoke();
        cookieData.readSignatureContent = readSignatureContent;
        cookieData.deserializeData(jsonObject);
        return cookieData;
    }

    protected abstract void deserializeData(JsonObject data);

    protected abstract void serializeData(JsonObject data);

    public ReadSignatureContent getSignatureContent() {
        return readSignatureContent;
    }

    public record ReadSignatureContent(
            String signData,
            String base64Signature
    ) {
        public boolean validateSignature(PublicKey publicKey, String algorithm) throws Exception {
            return RSASignatureUtil.verify(
                    signData.getBytes(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(base64Signature),
                    publicKey, algorithm
            );
        }
    }
}
