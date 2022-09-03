package moe.caa.multilogin.dataupgrade.newc.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.dataupgrade.oldc.OldConfig;
import moe.caa.multilogin.dataupgrade.oldc.OldYggdrasilConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 皮肤修复节点
 */
@Getter
@ToString
public class SkinRestorer {
    private final RestorerType restorer;
    private final Method method;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final Proxy proxy;

    SkinRestorer(OldConfig oc, OldYggdrasilConfig oldConfig) {
        this.restorer = RestorerType.valueOf(oldConfig.getSkinRestorer().name());
        this.method = Method.URL;
        this.timeout = oc.getServicesTimeOut();
        this.retry = oldConfig.getSkinRestorerRetry();
        this.retryDelay = 5000;
        this.proxy = new Proxy();
    }

    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("restorer").set(restorer.name());
        ret.node("method").set(method.name());
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
