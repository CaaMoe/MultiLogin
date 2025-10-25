package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public sealed class ReconnectCookieData extends CookieData permits ReconnectSpecifiedProfileIDCookieData {
    public int userID;

    @Override
    protected void deserializeData(JsonObject data) {
        userID = data.getAsJsonObject("data").getAsJsonPrimitive("user_id").getAsInt();
    }

    @Override
    protected void serializeData(JsonObject data) {
        JsonElement element = data.get("data");
        if (element == null || element instanceof JsonNull) {
            element = new JsonObject();
            data.add("data", element);
        }

        element.getAsJsonObject().addProperty("user_id", userID);
    }
}
