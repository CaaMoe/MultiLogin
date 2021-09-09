package moe.caa.multilogin.core.data.database.pool;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2 数据库链接池
 */
public class H2ConnectionPool implements ISQLConnectionPool {
    private JdbcConnectionPool cp;

    /**
     * 构建数据库链接池
     *
     * @param url      数据库链接
     * @param user     用户名
     * @param password 密码
     */
    public H2ConnectionPool(String url, String user, String password) {
        cp = JdbcConnectionPool.create(url, user, password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    @Override
    public void close() {
        cp.dispose();
    }
}
