package moe.caa.multilogin.core.data.databse.pool;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractConnectionPool {
    public abstract Connection getConnection() throws SQLException;

    public abstract void close();
}
