package moe.caa.multilogin.api.data;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.service.IService;

import java.util.UUID;

public interface IUserData {
    int getUserId();
    IService getService();
    UUID getLoginUUID();
    String getLoginUsername();
    boolean hasWhitelist();
    IProfileData getInitialProfileData();
    IProfileData getLinkToProfileData();
}
