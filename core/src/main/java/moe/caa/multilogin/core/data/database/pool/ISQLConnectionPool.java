package moe.caa.multilogin.core.data.database.pool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 抽象数据库链接池子
 */
public interface ISQLConnectionPool {

    /**
     * 获得链接对象
     *
     * @return 链接对象
     */
    Connection getConnection() throws SQLException;

    /**
     * 关闭链接
     */
    void close();
}
