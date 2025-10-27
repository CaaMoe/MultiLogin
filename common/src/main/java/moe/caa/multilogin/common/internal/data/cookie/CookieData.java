package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public sealed abstract class CookieData permits ReconnectCookieData, ReconnectSpecifiedProfileIDCookieData, RemoteAuthenticatedCookieData {
    public static final int COOKIE_EXPIRE_SECONDS = 10;
    private static Map<String, MethodHandle> typeCookieMap = Collections.emptyMap();
    private UUID cookieId = UUID.randomUUID();
    private Instant createAt = Instant.now();

    public static void init() throws IllegalAccessException, NoSuchMethodException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Map<String, MethodHandle> cookieTypeMap = collectTypeMap(lookup, CookieData.class);
        CookieData.typeCookieMap = Collections.unmodifiableMap(cookieTypeMap);
    }

    private static Map<String, MethodHandle> collectTypeMap(MethodHandles.Lookup lookup, Class<?> sealedClass) throws IllegalAccessException, NoSuchMethodException {
        Map<String, MethodHandle> cookieTypeMap = new HashMap<>();

        if (!Modifier.isAbstract(sealedClass.getModifiers())) {
            if (!sealedClass.isAnnotationPresent(CookieDataType.class)) {
                throw new IllegalStateException(sealedClass.getCanonicalName() + " is not annotated with " + CookieDataType.class.getCanonicalName());
            }

            cookieTypeMap.put(sealedClass.getAnnotation(CookieDataType.class).value(), lookup.unreflectConstructor(sealedClass.getConstructor()));
        }

        for (Class<?> permittedSubclass : sealedClass.getPermittedSubclasses()) {
            if (permittedSubclass.isSealed()) {
                cookieTypeMap.putAll(collectTypeMap(lookup, permittedSubclass));
            } else {
                if (!permittedSubclass.isAnnotationPresent(CookieDataType.class)) {
                    throw new IllegalStateException(permittedSubclass.getCanonicalName() + " is not annotated with " + CookieDataType.class.getCanonicalName());
                }

                cookieTypeMap.put(permittedSubclass.getAnnotation(CookieDataType.class).value(), lookup.unreflectConstructor(permittedSubclass.getConstructor()));
            }
        }
        return cookieTypeMap;
    }

    public static JsonObject serialize(CookieData cookieData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", cookieData.getClass().getAnnotation(CookieDataType.class).value());
        jsonObject.addProperty("cookie_id", cookieData.cookieId.toString());
        jsonObject.addProperty("create_at", cookieData.createAt.toString());
        cookieData.serializeData(jsonObject);
        return jsonObject;
    }


    public static CookieData deserialize(JsonObject data) throws Throwable {
        String type = data.getAsJsonPrimitive("type").getAsString();
        MethodHandle handle = typeCookieMap.get(type);
        if (handle == null) {
            throw new IllegalArgumentException("Invalid data type: " + type);
        }
        CookieData cookieData = (CookieData) handle.invoke();
        cookieData.cookieId = UUID.fromString(data.getAsJsonPrimitive("cookie_id").getAsString());
        cookieData.createAt = Instant.parse(data.getAsJsonPrimitive("create_at").getAsString());
        cookieData.deserializeData(data);
        return cookieData;
    }

    public UUID getCookieId() {
        return cookieId;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public boolean hasExpired() {
        return createAt.plusSeconds(COOKIE_EXPIRE_SECONDS).isBefore(Instant.now());
    }

    public abstract String getDescription();

    protected abstract void deserializeData(JsonObject data);

    protected abstract void serializeData(JsonObject data);
}
