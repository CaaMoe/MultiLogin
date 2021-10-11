package moe.caa.multilogin.core.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDataHandler {

    /**
     * 建表操作
     *
     * @param connection 链接
     * @throws SQLException 创表异常
     */
    void createIfNotExists(Connection connection) throws SQLException;
}
