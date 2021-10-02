package moe.caa.multilogin.core.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;
import java.util.UUID;

/**
 * 代表一位玩家的数据
 */
@Data
@AllArgsConstructor
public class User {
    private final UUID onlineUuid;
    private String currentName;
    private UUID redirectUuid;
    private String yggdrasilService;
    private boolean whitelist;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(onlineUuid, user.onlineUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlineUuid);
    }
}
