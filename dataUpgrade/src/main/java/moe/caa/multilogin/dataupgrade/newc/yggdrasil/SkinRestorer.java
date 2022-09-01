package moe.caa.multilogin.dataupgrade.newc.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

@Getter
@ToString
public class SkinRestorer {
    private final RestorerType restorer;
    private final Method method;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final Proxy proxy;

    private SkinRestorer(RestorerType restorer, Method method, int timeout, int retry, int retryDelay, Proxy proxy) {
        this.restorer = restorer;
        this.method = method;
        this.timeout = timeout;
        this.retry = retry;
        this.retryDelay = retryDelay;
        this.proxy = proxy;
    }

    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("restorer").set(restorer);
        ret.node("method").set(method);
        ret.node("timeout").set(timeout);
        ret.node("retry").set(retry);
        ret.node("retryDelay").set(retryDelay);
        ret.node("proxy").set(proxy.toYaml());
        return ret;
    }

    public enum Method {
        URL, UPLOAD
    }

    public enum RestorerType {
        OFF, LOGIN, ASYNC
    }
}
