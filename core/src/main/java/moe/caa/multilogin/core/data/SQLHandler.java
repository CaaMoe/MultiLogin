package moe.caa.multilogin.core.data;

import moe.caa.multilogin.core.util.UUIDSerializer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据库管理类
 */
public class SQLHandler {
    private static final String USER_DATA_TABLE_NAME = "user_data";
    private static final String ONLINE_UUID = "online_uuid";
    private static final String CURRENT_NAME = "current_name";
    private static final String REDIRECT_UUID = "redirect_name";
    private static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    private static final String WHITELIST = "whitelist";
    private static Connection conn;

    /**
     * 通过参数链接到数据库
     *
     * @param url 指定格式的参数
     */
    protected static void init(String[] url) throws Exception {
        if (url.length == 1) {
            conn = DriverManager.getConnection(url[0]);
        } else {
            conn = DriverManager.getConnection(url[0], url[1], url[2]);
        }

        try (Statement s = conn.createStatement()) {
            s.executeUpdate("" +
                    "CREATE TABLE IF NOT EXISTS " + USER_DATA_TABLE_NAME + "( " +
                    ONLINE_UUID + "  binary(16) PRIMARY KEY NOT NULL," +
                    CURRENT_NAME + "  VARCHAR(32)," +
                    REDIRECT_UUID + "  binary(16)," +
                    YGGDRASIL_SERVICE + "  VARCHAR(50)," +
                    WHITELIST + "  INTEGER)");
        }
    }

    /**
     * 通过主键online_uuid检索UserEntry数据
     *
     * @param uuid online_uuid
     * @return 检索到的UserEntry数据
     */
    public static UserEntry getUserEntryByOnlineUuid(UUID uuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, ONLINE_UUID
        ))) {
            ps.setString(1, uuid.toString());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                try {
                    return fromSQLResultSet(resultSet);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("以唯一标识符检索游戏数据时失败，数据疑似遭到损坏: %s", uuid.toString()), e);
                }
            }
            return null;
        }
    }

    /**
     * 通过主键current_name检索UserEntry数据
     *
     * @param name online_uuid
     * @return 检索到的UserEntry数据
     */
    public static List<UserEntry> getUserEntryByCurrentName(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                USER_DATA_TABLE_NAME, CURRENT_NAME
        ))) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            List<UserEntry> ret = new ArrayList<>();
            while (resultSet.next()) {
                try {
                    UserEntry add = fromSQLResultSet(resultSet);
                    ret.add(add);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("以用户名检索游戏数据时失败，数据疑似遭到损坏: %s", name), e);
                }
            }
            return ret;
        }
    }

    /**
     * 通过一个数据库检索结果生成一个数据对象
     *
     * @param resultSet 数据库检索结果
     * @return 数据对象
     */
    protected static UserEntry fromSQLResultSet(ResultSet resultSet) throws SQLException {
        return new UserEntry(
                UUIDSerializer.toUUID(resultSet.getBytes(1)),
                resultSet.getString(2),
                UUIDSerializer.toUUID(resultSet.getBytes(3)),
                resultSet.getString(4),
                resultSet.getInt(5));
    }

    /**
     * 将一个新的UserEntry写入到数据库中
     *
     * @param entry 新的UserEntry
     */
    public static void writeNewUserEntry(UserEntry entry) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                USER_DATA_TABLE_NAME, ONLINE_UUID, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST
        ))) {
            ps.setBytes(1, UUIDSerializer.uuidToByte(entry.getOnline_uuid()));
            ps.setString(2, entry.getCurrent_name());
            ps.setBytes(3, UUIDSerializer.uuidToByte(entry.getRedirect_uuid()));
            ps.setString(4, entry.getYggdrasil_service());
            ps.setInt(5, entry.getWhitelist());
            ps.executeUpdate();
        }
    }

    /**
     * 更新一个老的UserEntry
     *
     * @param entry 需要更新的UserEntry
     */
    public static void updateUserEntry(UserEntry entry) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST, ONLINE_UUID
        ))) {
            ps.setString(1, entry.getCurrent_name());
            ps.setBytes(2, UUIDSerializer.uuidToByte(entry.getRedirect_uuid()));
            ps.setString(3, entry.getYggdrasil_service());
            ps.setInt(4, entry.getWhitelist());
            ps.setBytes(5, UUIDSerializer.uuidToByte(entry.getOnline_uuid()));
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
