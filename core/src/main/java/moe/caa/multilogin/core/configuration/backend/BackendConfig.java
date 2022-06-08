package moe.caa.multilogin.core.configuration.backend;

import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class BackendConfig {
    private final BackendType backend;
    private final String ip;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final String tablePrefix;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BackendConfigSerializers implements TypeSerializer<BackendConfig> {
        @Getter
        private static final BackendConfigSerializers instance = new BackendConfigSerializers();

        @Override
        public BackendConfig deserialize(Type type, ConfigurationNode node) throws SerializationException {
            BackendType backend = node.node("backend").get(BackendType.class, BackendType.H2);
            String ip = node.node("ip").getString("127.0.0.1");
            int port = node.node("port").getInt(3306);
            String username = node.node("username").getString("root");
            String password = node.node("password").getString("root");
            String database = node.node("database").getString("multilogin");
            String tablePrefix = node.node("tablePrefix").getString("multilogin");
            return new BackendConfig(backend, ip, port, username, password, database, tablePrefix);
        }

        @Override
        public void serialize(Type type, @Nullable BackendConfig obj, ConfigurationNode node) {
            throw new UnsupportedOperationException();
        }
    }
}
