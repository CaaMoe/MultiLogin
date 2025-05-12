package moe.caa.multilogin.core.configuration.service.yggdrasil;

import moe.caa.multilogin.api.service.ServiceType;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import org.jetbrains.annotations.NotNull;

/**
 * 正版官方 Yggdrasil
 */
public class OfficialYggdrasilServiceConfig extends BaseYggdrasilServiceConfig {
    private final String customSessionServer;

    public OfficialYggdrasilServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat, boolean whitelist, SkinRestorerConfig skinRestorer, boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy, String customSessionServer) throws ConfException {
        super(id, name, initUUID, initNameFormat, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy);
        if (!customSessionServer.endsWith("/")) {
            customSessionServer = customSessionServer.concat("/");
        }
        this.customSessionServer = customSessionServer;
    }


    @Override
    protected String getAuthURL() {
	    String baseUrl = customSessionServer;
	    return baseUrl.concat("session/minecraft/hasJoined?username={0}&serverId={1}{2}");
    }

    @Override
    protected String getAuthPostContent() {
        throw new UnsupportedOperationException("get post content");
    }

    @Override
    protected String getAuthTrackIpContent() {
        return "&ip={0}";
    }

    @Override
    public HttpRequestMethod getHttpRequestMethod() {
        return HttpRequestMethod.GET;
    }

    @NotNull
    @Override
    public ServiceType getServiceType() {
        return ServiceType.OFFICIAL;
    }
}
