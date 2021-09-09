package moe.caa.multilogin.core.data.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL 链接池
 */
public class MysqlConnectionPool implements ISQLConnectionPool {
    private final HikariDataSource dataSource;

    /**
     * 构建链接池
     *
     * @param url      数据库链接
     * @param username 用户名
     * @param password 密码
     */
    public MysqlConnectionPool(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
