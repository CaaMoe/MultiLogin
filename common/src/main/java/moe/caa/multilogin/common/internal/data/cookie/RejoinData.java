package moe.caa.multilogin.common.internal.data.cookie;

import com.google.gson.JsonObject;

import java.util.UUID;

public final class RejoinData extends ExpirableData {
    private UUID uuid;

    @Override
    protected void deserializeData(JsonObject data) {
        uuid = UUID.fromString(data.getAsJsonObject("uuid").getAsString());
    }

    @Override
    protected void serializeData(JsonObject data) {
        data.addProperty("uuid", uuid.toString());
    }
}
