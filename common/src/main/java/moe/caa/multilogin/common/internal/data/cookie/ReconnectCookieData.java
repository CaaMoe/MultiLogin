package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import moe.caa.multilogin.common.internal.data.GameProfile;

@CookieDataType("reconnect")
public sealed class ReconnectCookieData extends CookieData permits ReconnectSpecifiedProfileIDCookieData {
    public int userID;
    public GameProfile authenticatedGameProfile;

    @Override
    public boolean isLocalSignature() {
        return true;
    }

    @Override
    protected void deserializeData(JsonObject data) {
        if (!data.has("data")) {
            data.add("data", new JsonObject());
        }
        userID = data.getAsJsonObject("data").getAsJsonPrimitive("user_id").getAsInt();
        authenticatedGameProfile = GameProfile.deserialize(data.getAsJsonObject("data").getAsJsonObject("authenticated_game_profile"));
    }

    @Override
    protected void serializeData(JsonObject data) {
        JsonElement element = data.get("data");
        if (element == null || element instanceof JsonNull) {
            element = new JsonObject();
            data.add("data", element);
        }

        element.getAsJsonObject().addProperty("user_id", userID);
        element.getAsJsonObject().add("authenticated_game_profile", authenticatedGameProfile.serialize());
    }
}
