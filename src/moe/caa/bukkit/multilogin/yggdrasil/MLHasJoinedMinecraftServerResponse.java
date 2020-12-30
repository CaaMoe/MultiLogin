package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.bukkit.multilogin.YggdrasilServiceSection;

public class MLHasJoinedMinecraftServerResponse extends HasJoinedMinecraftServerResponse {
    private transient YggdrasilServiceSection yggService;

    public MLHasJoinedMinecraftServerResponse(){

    }

    public YggdrasilServiceSection getYggService() {
        return yggService;
    }

    public void setYggService(YggdrasilServiceSection yggService) {
        this.yggService = yggService;
    }
}
