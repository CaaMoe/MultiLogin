package moe.caa.multilogin.core.data.database;

import java.sql.SQLException;
import java.sql.Statement;

public interface IDataHandler {

    void createIfNotExists(Statement statement) throws SQLException;
}
