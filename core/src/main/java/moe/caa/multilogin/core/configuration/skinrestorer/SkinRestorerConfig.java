package moe.caa.multilogin.core.configuration.skinrestorer;

import lombok.*;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class SkinRestorerConfig {
    private final SkinRestorerType restorer;
    private final SkinRestorerMethod method;
    private final int timeout;
    private final int retry;
    private final int retryDelay;
    private final ProxyConfig proxy;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SkinRestorerConfigSerializers implements TypeSerializer<SkinRestorerConfig> {
        @Getter
        private static final SkinRestorerConfigSerializers instance = new SkinRestorerConfigSerializers();

        @Override
        public SkinRestorerConfig deserialize(Type t, ConfigurationNode node) throws SerializationException {
            SkinRestorerType restorer = node.node("restorer").get(SkinRestorerType.class, SkinRestorerType.OFF);
            SkinRestorerMethod method = node.node("method").get(SkinRestorerMethod.class, SkinRestorerMethod.URL);
            int timeout = node.node("timeout").getInt(10000);
            int retry = node.node("retry").getInt(2);
            int retryDelay = node.node("retryDelay").getInt(5000);
            ProxyConfig proxy = node.node("proxy").get(ProxyConfig.class);
            return new SkinRestorerConfig(restorer, method, timeout, retry, retryDelay, proxy);
        }

        @Override
        public @Nullable SkinRestorerConfig emptyValue(Type specificType, ConfigurationOptions options) {
            return new SkinRestorerConfig(
                    SkinRestorerType.OFF,
                    SkinRestorerMethod.URL,
                    10000,
                    2,
                    5000,
                    ProxyConfig.ProxyConfigSerializers.getInstance().emptyValue(ProxyConfig.class, options)
            );
        }

        @Override
        public void serialize(Type type, @Nullable SkinRestorerConfig obj, ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException();
        }
    }
}
