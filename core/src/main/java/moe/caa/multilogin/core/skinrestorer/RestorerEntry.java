package moe.caa.multilogin.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RestorerEntry {
    private final UUID online_uuid;
    private String current_skin_url;
    private String restorer_data;

    public String getCurrentTextureValue() {
        JsonObject jo = JsonParser.parseString(restorer_data).getAsJsonObject();
        return jo.getAsJsonPrimitive("value").getAsString();
    }

    public String getCurrentTextureSignature() {
        JsonObject jo = JsonParser.parseString(restorer_data).getAsJsonObject();
        return jo.getAsJsonPrimitive("signature").getAsString();
    }
}
