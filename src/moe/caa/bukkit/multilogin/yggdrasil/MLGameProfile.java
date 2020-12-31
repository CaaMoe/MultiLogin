package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.GameProfile;
import moe.caa.bukkit.multilogin.YggdrasilServiceSection;

import java.util.UUID;

public class MLGameProfile extends GameProfile {
    private final YggdrasilServiceSection yggService;
    private final UUID onlineUuid;

    public MLGameProfile(UUID onlineUuid, UUID id, String name, YggdrasilServiceSection yggService) {
        super(id, name);
        this.yggService = yggService;
        this.onlineUuid = onlineUuid;
    }

    public YggdrasilServiceSection getYggService() {
        return yggService;
    }

    public UUID getOnlineUuid() {
        return onlineUuid;
    }
}
