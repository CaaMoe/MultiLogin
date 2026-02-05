package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class SqlConnectionPoolConfig {
    private final int maxPoolSize;
    private final long maxLifeTime;
    private final long keepaliveTime;
    private final int validationTimeout;

    public static SqlConnectionPoolConfig read(CommentedConfigurationNode node) {

        int maxPoolSize = node.node("maxPoolSize").getInt(20);
        long maxLifeTime = node.node("maxLifeTime").getLong(1_800_000L);
        long keepaliveTime = node.node("keepaliveTime").getLong(120_000L);
        int validationTimeout = node.node("validationTimeout").getInt(5000);

        return new SqlConnectionPoolConfig(maxPoolSize, maxLifeTime, keepaliveTime, validationTimeout);
    }

    public static SqlConnectionPoolConfig Default() {
        return new SqlConnectionPoolConfig(20, 1_800_000L, 120_000L, 5000);
    }
}
