package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 表示数据库配置
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class SqlConfig {
    private final SqlBackend backend;
    private final String ip;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final String tablePrefix;
    private final String connectUrl;

    public static SqlConfig read(CommentedConfigurationNode node) throws SerializationException {
        SqlBackend backend = node.node("backend").get(SqlBackend.class, SqlBackend.H2);
        String ip = node.node("ip").getString("127.0.0.1");
        int port = node.node("port").getInt(3306);
        String username = node.node("username").getString("root");
        String password = node.node("password").getString("root");
        String database = node.node("database").getString("multilogin");
        String tablePrefix = node.node("tablePrefix").getString("multilogin");
        String connectUrl = node.node("connectUrl").getString("");

        return new SqlConfig(backend, ip, port, username, password, database, tablePrefix, connectUrl);
    }

    public enum SqlBackend {
        H2, MYSQL
    }
}
