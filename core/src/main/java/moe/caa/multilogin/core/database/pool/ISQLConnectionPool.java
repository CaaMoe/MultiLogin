package moe.caa.multilogin.core.database.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ISQLConnectionPool {
    /**
     * 获得链接对象
     *
     * @return 链接对象
     */
    Connection getConnection() throws SQLException;

    /**
     * 获得该连接池名字
     */
    String name();

    /**
     * 关闭链接
     */
    void close();
}
