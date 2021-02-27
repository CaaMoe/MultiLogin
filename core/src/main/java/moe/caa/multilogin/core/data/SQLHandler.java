package moe.caa.multilogin.core.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据库管理类
 */
public class SQLHandler {
    private static Connection conn;

    private static final String USER_DATA_TABLE_NAME = "user_data";

    private static final String ONLINE_UUID = "online_uuid";
    private static final String CURRENT_NAME = "current_name";
    private static final String REDIRECT_UUID = "redirect_name";
    private static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    private static final String WHITELIST = "whitelist";

    /**
     * 通过参数链接到数据库
     * @param url 指定格式的参数
     */
    protected static void init(String[] url) throws Exception {
        if(url.length == 1){
            conn = DriverManager.getConnection(url[0]);
        } else {
            conn = DriverManager.getConnection(url[0], url[1], url[2]);
        }

        try (Statement s = conn.createStatement()){
            s.executeUpdate("" +
                    "CREATE TABLE IF NOT EXISTS "     + USER_DATA_TABLE_NAME + "                                  ( " +
                    ONLINE_UUID                       + "  VARCHAR(255) PRIMARY KEY NOT NULL,                       " +
                    CURRENT_NAME                      + "  VARCHAR(255)                     ,                       " +
                    REDIRECT_UUID                     + "  VARCHAR(255)                     ,                       " +
                    YGGDRASIL_SERVICE                 + "  VARCHAR(255)                     ,                       " +
                    WHITELIST                         + "  INTEGER                                                  )");
        }
    }

    /**
     * 通过主键online_uuid检索UserEntry数据
     * @param uuid online_uuid
     * @return 检索到的UserEntry数据
     */
    public static UserEntry getUserEntryByOnlineUuid(UUID uuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?" ,
                USER_DATA_TABLE_NAME, ONLINE_UUID
        ))){
            ps.setString(1, uuid.toString());
            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()){
                try {
                    return UserEntry.fromSQLResultSet(resultSet);
                } catch (Exception e){
                    throw new RuntimeException(String.format("以唯一标识符检索游戏数据时失败，数据疑似遭到损坏: %s", uuid.toString()), e);
                }
            }
            return null;
        }
    }

    /**
     * 通过主键current_name检索UserEntry数据
     * @param name online_uuid
     * @return 检索到的UserEntry数据
     */
    public static List<UserEntry> getUserEntryByCurrentName(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?" ,
                USER_DATA_TABLE_NAME, CURRENT_NAME
        ))){
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            List<UserEntry> ret = new ArrayList<>();
            while (resultSet.next()){
                try {
                    UserEntry add = UserEntry.fromSQLResultSet(resultSet);
                    ret.add(add);
                } catch (Exception e){
                    throw new RuntimeException(String.format("以用户名检索游戏数据时失败，数据疑似遭到损坏: %s", name), e);
                }
            }
            return ret;
        }
    }

    /**
     * 将一个新的UserEntry写入到数据库中
     * @param entry 新的UserEntry
     */
    public static void writeNewUserEntry(UserEntry entry) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)" ,
                USER_DATA_TABLE_NAME, ONLINE_UUID, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST
        ))){
            entry.writeNewUserEntryPreparedStatement(ps);
            ps.executeUpdate();
        }
    }

    /**
     * 更新一个老的UserEntry
     * @param entry 需要更新的UserEntry
     */
    public static void updateUserEntry(UserEntry entry) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? " ,
                USER_DATA_TABLE_NAME, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST, ONLINE_UUID
        ))){
            entry.updateUserEntryPreparedStatement(ps);
            ps.executeUpdate();
        }
    }

    /**
     * 数据库关闭链接
     */
    protected static void close() throws SQLException {
        conn.close();
    }
}
