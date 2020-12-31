package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.GameProfile;
import moe.caa.bukkit.multilogin.YggdrasilServiceSection;

import java.util.UUID;

public class MLGameProfile extends GameProfile {
    private final YggdrasilServiceSection yggService;

    public MLGameProfile(UUID id, String name, YggdrasilServiceSection yggService) {
        super(id, name);
        this.yggService = yggService;
    }

    public YggdrasilServiceSection getYggService() {
        return yggService;
    }
}
