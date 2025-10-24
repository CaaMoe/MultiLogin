package moe.caa.multilogin.common.internal.cookie;

import com.google.gson.JsonObject;

import java.time.Instant;

public abstract sealed class ExpirableData extends CookieData permits RemoteAuthenticatedData {
    private Instant expiresAt;


    @Override
    protected void deserializeData(JsonObject data) {
        expiresAt = Instant.parse(data.getAsJsonPrimitive("expires_at").getAsString());
    }

    @Override
    protected void serializeData(JsonObject data) {
        data.addProperty("expires_at", expiresAt.toString());
    }
}
