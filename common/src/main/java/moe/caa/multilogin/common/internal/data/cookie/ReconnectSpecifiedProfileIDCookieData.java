package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;

@CookieDataType("reconnect_specified_profile_id")
public final class ReconnectSpecifiedProfileIDCookieData extends ReconnectCookieData {
    public int specifiedProfileID;

    @Override
    protected void deserializeData(JsonObject data) {
        super.deserializeData(data);
        specifiedProfileID = data.getAsJsonObject("data").getAsJsonPrimitive("specified_profile_id").getAsInt();
    }

    @Override
    protected void serializeData(JsonObject data) {
        super.serializeData(data);
        data.get("data").getAsJsonObject().addProperty("specified_profile_id", specifiedProfileID);
    }
}
