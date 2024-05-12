package moe.caa.multilogin.core.configuration.service.yggdrasil;

import lombok.Getter;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
public abstract class BaseYggdrasilServiceConfig extends BaseServiceConfig {
    private final boolean trackIp;
    private final int timeout;
    private final int retry;
    private final long retryDelay;
    private final ProxyConfig authProxy;

    protected BaseYggdrasilServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat, boolean whitelist, SkinRestorerConfig skinRestorer,
                                         boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy) throws ConfException {
        super(id, name, initUUID, initNameFormat, whitelist, skinRestorer);
        this.trackIp = trackIp;
        this.timeout = timeout;
        this.retry = retry;
        this.retryDelay = retryDelay;
        this.authProxy = authProxy;
    }


    /**
     * 生成验证 URL
     */
    public String generateAuthURL(String username, String serverId, String ip) {
        return ValueUtil.transPapi(getAuthURL(),
                new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                new Pair<>("ip", generateTraceIpContent(ip)));
    }


    /**
     * 生成验证 POST 内容
     */
    public String generateAuthPostContent(String username, String serverId, String ip) {
        return ValueUtil.transPapi(getAuthPostContent(),
                new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                new Pair<>("ip", generateTraceIpContent(ip)));
    }

    private String generateTraceIpContent(String ip) {
        if (!trackIp) {
            return "";
        }
        if (ValueUtil.isEmpty(ip)) {
            return "";
        }
        String trackIpContent = getAuthTrackIpContent();
        if (ValueUtil.isEmpty(trackIpContent)) {
            return "";
        }
        return ValueUtil.transPapi(trackIpContent,
                new Pair<>("ip", ip));
    }

    /**
     * 生成验证 URL
     */
    protected abstract String getAuthURL();

    /**
     * 生成验证 POST 内容
     */
    protected abstract String getAuthPostContent();

    /**
     * 生成验证 IP 内容
     */
    protected abstract String getAuthTrackIpContent();

    /**
     * 返回请求类型
     */
    public abstract HttpRequestMethod getHttpRequestMethod();

    public enum HttpRequestMethod {
        GET, POST
    }
}
