package moe.caa.multilogin.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 游戏档案
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameProfile {
    private UUID id;
    private String name;
    private Map<String, Property> propertyMap;

    @Override
    public GameProfile clone() {
        GameProfile response = new GameProfile(id, name, new HashMap<>());
        for (Map.Entry<String, Property> entry : propertyMap.entrySet()) {
            response.propertyMap.put(entry.getKey(), entry.getValue().clone());
        }
        return response;
    }
}
