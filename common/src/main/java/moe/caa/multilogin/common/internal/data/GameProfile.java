package moe.caa.multilogin.common.internal.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GameProfile(
        UUID uuid,
        String username,
        List<Property> properties
) {
    public static GameProfile deserialize(JsonObject jsonObject) {
        UUID uuid = UUID.fromString(jsonObject.getAsJsonPrimitive("uuid").getAsString());
        String username = jsonObject.getAsJsonPrimitive("username").getAsString();
        List<Property> properties = new ArrayList<>();

        for (JsonElement element : jsonObject.getAsJsonArray("properties")) {
            JsonObject asJsonObject = element.getAsJsonObject();
            properties.add(new Property(
                    asJsonObject.getAsJsonPrimitive("name").getAsString(),
                    asJsonObject.getAsJsonPrimitive("value").getAsString(),
                    asJsonObject.getAsJsonPrimitive("signature").getAsString()
            ));
        }
        return new GameProfile(uuid, username, properties);
    }

    public record Property(
            String name,
            String value,
            String signature
    ) {
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("username", username);

        JsonArray array = new JsonArray();
        for (Property property : properties) {
            JsonObject propertyDataJsonObject = new JsonObject();
            propertyDataJsonObject.addProperty("name", property.name);
            propertyDataJsonObject.addProperty("value", property.value);
            propertyDataJsonObject.addProperty("signature", property.signature);
            array.add(propertyDataJsonObject);
        }
        object.add("properties", array);
        return object;
    }
}