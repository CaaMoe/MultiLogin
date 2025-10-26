package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import moe.caa.multilogin.common.internal.data.GameProfile;

@CookieDataType("remote_authenticated")
public final class RemoteAuthenticatedCookieData extends CookieData {
    public String serviceID;
    public GameProfile authenticatedGameProfile;

    @Override
    public String getDescription() {
        return "remote authenticated.";
    }

    @Override
    protected void deserializeData(JsonObject data) {
        if (!data.has("data")) {
            data.add("data", new JsonObject());
        }
        authenticatedGameProfile = GameProfile.deserialize(data.getAsJsonObject("data").getAsJsonObject("authenticated_game_profile"));
        serviceID = data.getAsJsonObject("data").getAsJsonPrimitive("remote_service_id").getAsString();
    }

    @Override
    protected void serializeData(JsonObject data) {
        JsonElement element = data.get("data");
        if (element == null || element instanceof JsonNull) {
            element = new JsonObject();
            data.add("data", element);
        }

        element.getAsJsonObject().add("authenticated_game_profile", authenticatedGameProfile.serialize());
        element.getAsJsonObject().addProperty("remote_service_id", serviceID);
    }
}
