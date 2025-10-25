package moe.caa.multilogin.common.internal.config;

import moe.caa.multilogin.common.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class ReconnectFeature extends Configuration {
    public final ConfigurationValue<Boolean> enable;
    public final ConfigurationValue<String> reconnectAddress;

    public ReconnectFeature() {
        this.enable = boolOpt(NodePath.path("enable"), true);
        this.reconnectAddress = stringOpt(NodePath.path("reconnect-address"), "");
    }
}
