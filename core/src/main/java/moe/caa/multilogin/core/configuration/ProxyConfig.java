package moe.caa.multilogin.core.configuration;

import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProxyConfigSerializers implements TypeSerializer<ProxyConfig> {
        @Getter
        private static final ProxyConfigSerializers instance = new ProxyConfigSerializers();

        @Override
        public ProxyConfig deserialize(Type t, ConfigurationNode node) throws SerializationException {
            Proxy.Type type = node.node("type").get(Proxy.Type.class, Proxy.Type.DIRECT);
            String hostname = node.node("hostname").getString("127.0.0.1");
            int port = node.node("port").getInt(1080);
            String username = node.node("username").getString("");
            String password = node.node("username").getString("");
            return new ProxyConfig(type, hostname, port, username, password);
        }

        @Override
        public @Nullable ProxyConfig emptyValue(Type specificType, ConfigurationOptions options) {
            return new ProxyConfig(Proxy.Type.DIRECT, "127.0.0.1", 1080, "", "");
        }

        @Override
        public void serialize(Type type, @Nullable ProxyConfig obj, ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException();
        }
    }
}
