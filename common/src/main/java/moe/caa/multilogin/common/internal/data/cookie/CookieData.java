package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public sealed abstract class CookieData permits ExpirableData {
    private static Map<String, MethodHandle> typeCookieMap = Collections.emptyMap();

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

    public static JsonObject serialize(CookieData cookieData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", cookieData.getClass().getAnnotation(CookieDataType.class).type());
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
        cookieData.deserializeData(data);
        return cookieData;
    }

    protected abstract void deserializeData(JsonObject data);

    protected abstract void serializeData(JsonObject data);
}
