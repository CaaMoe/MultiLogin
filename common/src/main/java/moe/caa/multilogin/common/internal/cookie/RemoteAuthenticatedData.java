package moe.caa.multilogin.common.internal.cookie;

@CookieDataType(type = "remote_authenticated")
public final class RemoteAuthenticatedData extends ExpirableData {
    public String loginMethod;

}
