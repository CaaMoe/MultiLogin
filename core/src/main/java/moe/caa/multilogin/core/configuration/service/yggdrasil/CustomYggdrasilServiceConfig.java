package moe.caa.multilogin.core.configuration.service.yggdrasil;

import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import moe.caa.multilogin.core.configuration.service.ServiceType;

/**
 * 自定义 Yggdrasil
 */
public class CustomYggdrasilServiceConfig extends BaseYggdrasilServiceConfig {
    private final String url;
    private final String postContent;
    private final String trackIpContent;
    private final HttpRequestMethod method;

    public CustomYggdrasilServiceConfig(int id, String name, InitUUID initUUID, boolean whitelist, SkinRestorerConfig skinRestorer, boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy, String url, String postContent, String trackIpContent, HttpRequestMethod method) throws ConfException {
        super(id, name, initUUID, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy);
        this.url = url;
        this.postContent = postContent;
        this.trackIpContent = trackIpContent;
        this.method = method;
    }


    @Override
    protected String getAuthURL() {
        return url;
    }

    @Override
    protected String getAuthPostContent() {
        return postContent;
    }

    @Override
    protected String getAuthTrackIpContent() {
        return trackIpContent;
    }

    @Override
    public HttpRequestMethod getHttpRequestMethod() {
        return method;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CUSTOM_YGGDRASIL;
    }
}
