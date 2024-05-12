package moe.caa.multilogin.core.auth.service.yggdrasil.serialize;

import com.google.gson.*;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.profile.Property;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.auth.service.yggdrasil.UnmodifiableGameProfile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * GameProfile 的 GSON 序列化程序
 */
@NoArgsConstructor
public class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {

    @Override
    public GameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        GameProfile ret = new GameProfile();
        HashMap<String, Property> propertyMap = new HashMap<>();
        ret.setPropertyMap(propertyMap);
        if (json.isJsonObject()) {
            JsonObject root = json.getAsJsonObject();
            ret.setId(ValueUtil.getUuidOrNull(root.get("id").getAsString()));
            if (root.has("name"))
                ret.setName(root.get("name").getAsString());
            JsonElement propertiesJsonElement = root.get("properties");
            if (propertiesJsonElement != null) {
                if (propertiesJsonElement.isJsonObject()) {
                    JsonObject object = propertiesJsonElement.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        if (entry.getValue().isJsonArray()) {
                            for (JsonElement ignored : entry.getValue().getAsJsonArray()) {
                                propertyMap.put(entry.getKey(), context.deserialize(ignored, Property.class));
                            }
                        }
                    }
                } else if (propertiesJsonElement.isJsonArray()) {
                    for (JsonElement element : propertiesJsonElement.getAsJsonArray()) {
                        Property value = context.deserialize(element, Property.class);
                        propertyMap.put(value.getName(), value);
                    }
                }
            }
        }
        return UnmodifiableGameProfile.unmodifiable(ret);
    }

    @Override
    public JsonElement serialize(GameProfile src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject ret = new JsonObject();
        ret.addProperty("id", src.getId().toString().replace("-", ""));
        ret.addProperty("name", src.getName());
        JsonArray propertiesJsonArray = new JsonArray();
        ret.add("properties", propertiesJsonArray);
        for (var entry : src.getPropertyMap().entrySet()) {
            propertiesJsonArray.add(context.serialize(entry.getValue(), Property.class));
        }
        return ret;
    }
}
