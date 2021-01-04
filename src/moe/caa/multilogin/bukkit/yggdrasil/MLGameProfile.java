package moe.caa.multilogin.bukkit.yggdrasil;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.core.YggdrasilService;

import java.util.UUID;

public class MLGameProfile extends GameProfile {
    private final YggdrasilService yggService;
    private final UUID onlineUuid;

    public MLGameProfile(UUID onlineUuid, UUID id, String name, YggdrasilService yggService) {
        super(id, name);
        this.yggService = yggService;
        this.onlineUuid = onlineUuid;
    }

    public YggdrasilService getYggService() {
        return yggService;
    }

    public UUID getOnlineUuid() {
        return onlineUuid;
    }
}
