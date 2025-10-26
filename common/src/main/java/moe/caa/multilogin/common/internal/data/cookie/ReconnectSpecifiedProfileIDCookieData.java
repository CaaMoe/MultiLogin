package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import moe.caa.multilogin.common.internal.data.GameProfile;

@CookieDataType("local_reconnect_specified_profile")
public final class ReconnectSpecifiedProfileIDCookieData extends CookieData {
    public int userID;
    public GameProfile authenticatedGameProfile;
    public int specifiedProfileID;

    @Override
    public String getDescription() {
        return "local reconnection, specified login profile.";
    }

    @Override
    protected void deserializeData(JsonObject data) {
        if (!data.has("data")) {
            data.add("data", new JsonObject());
        }
        userID = data.getAsJsonObject("data").getAsJsonPrimitive("user_id").getAsInt();
        authenticatedGameProfile = GameProfile.deserialize(data.getAsJsonObject("data").getAsJsonObject("authenticated_game_profile"));
        specifiedProfileID = data.getAsJsonObject("data").getAsJsonPrimitive("specified_profile_id").getAsInt();
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
        data.get("data").getAsJsonObject().addProperty("specified_profile_id", specifiedProfileID);
    }
}
