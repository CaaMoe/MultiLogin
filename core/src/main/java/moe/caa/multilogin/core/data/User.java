package moe.caa.multilogin.core.data;

import moe.caa.multilogin.core.yggdrasil.YggdrasilService;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.util.Objects;
import java.util.UUID;

public class User {
    public final UUID onlineUuid;
    public final transient YggdrasilService service;
    public String currentName;
    public UUID redirectUuid;
    public String yggdrasilService;
    public boolean whitelist;

    public User(UUID onlineUuid, String currentName, UUID redirectUuid, String yggdrasilService, boolean whitelist) {
        this.onlineUuid = onlineUuid;
        this.currentName = currentName;
        this.redirectUuid = redirectUuid;
        this.yggdrasilService = yggdrasilService;
        this.whitelist = whitelist;
        this.service = YggdrasilServicesHandler.getService(yggdrasilService);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User entry = (User) o;
        return Objects.equals(onlineUuid, entry.onlineUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlineUuid);
    }
}
