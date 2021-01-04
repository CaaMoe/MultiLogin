package moe.caa.multilogin.bukkit.yggdrasil;

import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.core.YggdrasilService;

public class MLHasJoinedMinecraftServerResponse extends HasJoinedMinecraftServerResponse {
    private transient YggdrasilService yggService;

    public MLHasJoinedMinecraftServerResponse(){
    }

    public YggdrasilService getYggService() {
        return yggService;
    }

    public void setYggService(YggdrasilService yggService) {
        this.yggService = yggService;
    }
}
