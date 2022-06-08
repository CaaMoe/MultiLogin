package moe.caa.multilogin.core.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL 链接池
 */
public class MysqlConnectionPool implements ISQLConnectionPool {
    public static final String defaultUrl = "jdbc:mysql://{0}:{1}/{2}?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
    private final HikariDataSource dataSource;

    public MysqlConnectionPool(String ip, int port, String database, String username, String password, String url) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        url = url.replace("{0}", ip).replace("{1}", String.valueOf(port)).replace("{2}", database);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(config);
    }

    public MysqlConnectionPool(String ip, int port, String database, String username, String password) throws ClassNotFoundException {
        this(ip, port, database, username, password, defaultUrl);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public String name() {
        return "MySQL";
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
