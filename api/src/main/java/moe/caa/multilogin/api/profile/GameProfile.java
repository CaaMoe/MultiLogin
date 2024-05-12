package moe.caa.multilogin.api.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameProfile that = (GameProfile) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(propertyMap, that.propertyMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, propertyMap);
    }
}
