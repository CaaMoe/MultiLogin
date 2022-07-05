package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class SkinRestorerConfig {
    private final RestorerType restorer;
    private final Method method;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final ProxyConfig proxy;

    public enum RestorerType {
        OFF, LOGIN, ASYNC
    }

    public enum Method {
        URL, UPLOAD
    }
}
