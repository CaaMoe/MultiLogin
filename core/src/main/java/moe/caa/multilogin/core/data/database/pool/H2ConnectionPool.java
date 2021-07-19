package moe.caa.multilogin.core.data.database.pool;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class H2ConnectionPool implements ISQLConnectionPool {
    JdbcConnectionPool cp;

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
