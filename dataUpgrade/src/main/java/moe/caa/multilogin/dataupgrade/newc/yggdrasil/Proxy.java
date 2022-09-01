package moe.caa.multilogin.dataupgrade.newc.yggdrasil;

import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

@Getter
@ToString
public class Proxy {
    private final java.net.Proxy.Type type;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public Proxy() {
        this.type = java.net.Proxy.Type.DIRECT;
        this.hostname = "127.0.0.1";
        this.port = 1080;
        this.username = "";
        this.password = "";
    }

    public CommentedConfigurationNode toYaml() throws SerializationException {
        CommentedConfigurationNode ret = CommentedConfigurationNode.root();
        ret.node("type").set(type);
        ret.node("hostname").set(hostname);
        ret.node("port").set(port);
        ret.node("username").set(username);
        ret.node("password").set(password);
        return ret;
    }
}
