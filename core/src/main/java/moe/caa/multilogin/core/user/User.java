package moe.caa.multilogin.core.user;

import lombok.Data;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.util.Objects;
import java.util.UUID;

/**
 * 代表一位玩家的数据
 */
@Data
public class User {
    private final UUID onlineUuid;
    private String currentName;
    private UUID redirectUuid;
    private String yggdrasilService;
    private boolean whitelist;
    private transient YggdrasilService service;

    public User(UUID onlineUuid, String currentName, UUID redirectUuid, String yggdrasilService, boolean whitelist) {
        this.onlineUuid = onlineUuid;
        this.currentName = currentName;
        this.redirectUuid = redirectUuid;
        this.yggdrasilService = yggdrasilService;
        this.whitelist = whitelist;
    }

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
