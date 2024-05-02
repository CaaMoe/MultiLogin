package moe.caa.multilogin.api.data;

import java.util.UUID;

public interface IProfileData {
    int getProfileId();
    UUID getProfileUUID();
    String getProfileUsername();
}
