package moe.caa.multilogin.api.data;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.service.IService;

public interface MultiLoginPlayerData {
    GameProfile getOnlineProfile();

    IService getLoginService();
}
