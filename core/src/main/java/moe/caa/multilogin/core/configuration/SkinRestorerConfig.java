package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 表示一个皮肤修复配置
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class SkinRestorerConfig {
    private final RestorerType restorer;
    private final Method method;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final ProxyConfig proxy;

    public static SkinRestorerConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
        RestorerType restorer = node.node("restorer").get(RestorerType.class, RestorerType.OFF);
        Method method = node.node("method").get(Method.class, Method.URL);
        int timeout = node.node("timeout").getInt(10000);
        int retry = node.node("retry").getInt(2);
        int retryDelay = node.node("retryDelay").getInt(5000);
        ProxyConfig proxy = ProxyConfig.read(node.node("proxy"));

        return new SkinRestorerConfig(restorer, method, timeout, retry, retryDelay, proxy);
    }

    public enum RestorerType {
        OFF, LOGIN, ASYNC
    }

    public enum Method {
        URL, UPLOAD
    }
}
