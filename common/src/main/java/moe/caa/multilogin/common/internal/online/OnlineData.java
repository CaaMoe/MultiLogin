package moe.caa.multilogin.common.internal.online;

public abstract class OnlineData {
    public final OnlineUser onlineUser;
    public final OnlineProfile onlineProfile;

    protected OnlineData(OnlineUser onlineUser, OnlineProfile onlineProfile) {
        this.onlineUser = onlineUser;
        this.onlineProfile = onlineProfile;
    }
}
