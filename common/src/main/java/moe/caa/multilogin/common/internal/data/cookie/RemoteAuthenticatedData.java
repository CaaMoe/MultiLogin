package moe.caa.multilogin.common.internal.data.cookie;

@CookieDataType(type = "remote_authenticated")
public final class RemoteAuthenticatedData extends ExpirableData {
    public String loginMethod;

}
