package moe.caa.multilogin.core.auth.yggdrasil.serialize;

import com.google.gson.*;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.util.ValueUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * HasJoinedResponse 的 GSON 序列化程序
 *
 * @see HasJoinedResponse
 */
@NoArgsConstructor
public class HasJoinedResponseSerializer implements JsonSerializer<HasJoinedResponse>, JsonDeserializer<HasJoinedResponse> {

    @Override
    public HasJoinedResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var ret = new HasJoinedResponse();
        var propertyMap = new HashMap<String, Property>();
        ret.setPropertyMap(propertyMap);
        if (json.isJsonObject()) {
            var root = json.getAsJsonObject();
            ret.setId(ValueUtil.getUuidOrNull(root.get("id").getAsString()));
            if (root.has("name"))
                ret.setName(root.get("name").getAsString());
            var propertiesJsonElement = root.get("properties");
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
        return ret;
    }

    @Override
    public JsonElement serialize(HasJoinedResponse src, Type typeOfSrc, JsonSerializationContext context) {
        var ret = new JsonObject();
        ret.addProperty("id", src.getId().toString().replace("-", ""));
        ret.addProperty("name", src.getName());
        var propertiesJsonArray = new JsonArray();
        ret.add("properties", propertiesJsonArray);
        for (var entry : src.getPropertyMap().entrySet()) {
            propertiesJsonArray.add(context.serialize(entry.getValue(), Property.class));
        }
        return ret;
    }
}
