package moe.caa.multilogin.core.impl;

import java.sql.Connection;
import java.sql.SQLException;

public interface ISQLConnectionPool {
    Connection getConnection() throws SQLException;

    void close();
}
