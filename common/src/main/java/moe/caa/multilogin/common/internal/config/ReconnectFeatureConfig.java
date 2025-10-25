package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import moe.caa.multilogin.common.internal.util.StringUtil;
import org.spongepowered.configurate.NodePath;

import java.net.InetSocketAddress;
import java.util.Optional;

public class ReconnectFeatureConfig extends Configuration {
    public final ConfigurationValue<Boolean> enable;
    public final ConfigurationValue<Optional<InetSocketAddress>> reconnectAddress;

    public ReconnectFeatureConfig() {
        this.enable = boolOpt(NodePath.path("enable"), true);
        this.reconnectAddress = raw(NodePath.path("reconnect-address"), configurationNode -> {
            String string = configurationNode.getString();
            if (StringUtil.isNullOrEmpty(string)) return Optional.empty();
            String[] split = string.split(":");
            return Optional.of(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        });
    }
}
