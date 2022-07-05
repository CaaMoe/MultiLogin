package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class YggdrasilServiceConfig {
    private final int id;
    private final String name;

    private final String url;
    private final HttpRequestMethod method;
    private final String ipContents;
    private final String postContent;
    private final boolean passIp;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final ProxyConfig proxy;

    private final InitUUID initUUID;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final boolean compulsoryUsername;
    private final SkinRestorerConfig skinRestorer;

    public enum HttpRequestMethod {
        GET, POST
    }

    public enum InitUUID {
        DEFAULT, OFFLINE, RANDOM
    }
}
