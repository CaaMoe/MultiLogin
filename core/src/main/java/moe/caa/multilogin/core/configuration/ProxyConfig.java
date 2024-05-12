package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * 表示一个代理配置
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class ProxyConfig {
    private final Proxy.Type type;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public static ProxyConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
        Proxy.Type type = node.node("type").get(Proxy.Type.class, Proxy.Type.DIRECT);
        String hostname = node.node("hostname").getString("127.0.0.1");
        int port = node.node("port").getInt(1080);
        String username = node.node("username").getString("");
        String password = node.node("password").getString("");

        return new ProxyConfig(type, hostname, port, username, password);
    }

    public Proxy getProxy() {
        if (type == Proxy.Type.DIRECT) return Proxy.NO_PROXY;
        return new Proxy(type, new InetSocketAddress(hostname, port));
    }

    public Authenticator getProxyAuthenticator() {
        return (route, response) -> {
            if (ValueUtil.isEmpty(username)) return null;
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
    }
}
