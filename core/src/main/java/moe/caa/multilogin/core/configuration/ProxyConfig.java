package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.util.ValueUtil;
import okhttp3.Authenticator;
import okhttp3.Credentials;

import java.net.InetSocketAddress;
import java.net.Proxy;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class ProxyConfig {
    private final Proxy.Type type;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public Proxy getProxy() {
        return new Proxy(type, new InetSocketAddress(hostname, port));
    }

    public Authenticator getProxyAuthenticator() {
        return (route, response) -> {
            if (!ValueUtil.isEmpty(username)) return null;
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
    }
}
