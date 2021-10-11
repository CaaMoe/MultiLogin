package moe.caa.multilogin.core.data.database.handle;

import moe.caa.multilogin.core.data.database.IDataHandler;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static moe.caa.multilogin.core.data.database.SQLManager.USER_DATA_TABLE_NAME;

/**
 * 玩家数据管理类
 */
public class UserDataHandler implements IDataHandler {

    private final SQLManager sqlManager;

    /**
     * 构建这个玩家数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public UserDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    @Override
    public void createIfNotExists(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + USER_DATA_TABLE_NAME + "( " +
                sqlManager.getCore().getSetting().getDatabase_user_data_table_field_online_uuid() + " BINARY(16) PRIMARY KEY NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_user_data_table_field_current_name() + " VARCHAR(100) NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_user_data_table_field_redirect_uuid() + " BINARY(16) NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_user_data_table_field_yggdrasil_service() + " VARCHAR(100) NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_user_data_table_field_whitelist() + " BOOL NOT NULL)")) {
            ps.executeUpdate();
        }
    }

    /**
     * 合成玩家数据对象
     *
     * @param resultSet 数据库检索结果
     * @return 玩家数据对象
     * @throws SQLException 读取异常
     */
    private User getUser(ResultSet resultSet) throws SQLException {
        return new User(
                ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                resultSet.getString(2),
                ValueUtil.bytesToUuid(resultSet.getBytes(3)),
                resultSet.getString(4),
                resultSet.getBoolean(5)
        );
    }

    /**
     * 合成玩家数据对象列表
     *
     * @param resultSet 数据库检索结果
     * @return 玩家数据对象列表
     * @throws SQLException 读取异常
     */
    private List<User> getUsers(ResultSet resultSet) throws SQLException {
        List<User> ret = new ArrayList<>();
        while (resultSet.next()) {
            User add = getUser(resultSet);
            ret.add(add);
        }
        return ret;
    }

    /**
     * 通过主键 onlineUuid 获取玩家数据对象
     *
     * @param uuid 在线 uuid
     * @return 玩家数据对象
     * @throws SQLException 读取异常
     */
    public User getUserEntryByOnlineUuid(UUID uuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_online_uuid()
        ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(uuid));
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return getUser(resultSet);
                }
            }
            return null;
        }
    }

    /**
     * 通过 redirectUuid 获取玩家数据对象
     *
     * @param uuid 在线 uuid
     * @return 玩家数据对象列表
     * @throws SQLException 读取异常
     */
    public List<User> getUserEntryByRedirectUuid(UUID uuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                     USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_redirect_uuid()
             ))) {

            ps.setBytes(1, ValueUtil.uuidToBytes(uuid));
            try (ResultSet resultSet = ps.executeQuery()) {
                return getUsers(resultSet);
            }
        }
    }

    /**
     * 通过 currentName 获取玩家数据对象
     *
     * @param name 当前name
     * @return 玩家数据对象列表
     * @throws SQLException 读取异常
     */
    public List<User> getUserEntryByCurrentName(String name) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_current_name()
        ))) {
            ps.setString(1, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                return getUsers(resultSet);
            }
        }
    }

    /**
     * 获取所有具有白名单的玩家
     *
     * @return 玩家数据对象列表
     * @throws SQLException 读取异常
     */
    public List<User> getUserEntryWhereHaveWhitelist() throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_whitelist()
        ))) {
            ps.setBoolean(1, true);
            try (ResultSet resultSet = ps.executeQuery()) {
                return getUsers(resultSet);
            }
        }
    }

    /**
     * 通过 currentName 获取 YggdrasilService 实例
     *
     * @param name 当前name
     * @return YggdrasilService 列表
     * @throws SQLException 读取异常
     */
    public Set<YggdrasilService> getYggdrasilServiceByCurrentName(String name) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format("SELECT %s FROM %s WHERE %s = ?",
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_yggdrasil_service(), USER_DATA_TABLE_NAME,
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_current_name()
             ))) {
            ps.setString(1, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                Set<YggdrasilService> ret = new HashSet<>();
                while (resultSet.next()) {
                    YggdrasilService yggdrasilService = sqlManager.getCore().getYggdrasilServicesHandler().getYggdrasilService(resultSet.getString(1));
                    if (yggdrasilService != null) ret.add(yggdrasilService);
                }
                return ret;
            }
        }
    }

    /**
     * 写入一条新的玩家数据
     *
     * @param entry 玩家数据
     * @throws SQLException 写入异常
     */
    public void writeNewUserEntry(User entry) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                     USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_online_uuid(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_current_name(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_redirect_uuid(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_yggdrasil_service(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_whitelist()
             ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(entry.getOnlineUuid()));
            ps.setString(2, entry.getCurrentName());
            ps.setBytes(3, ValueUtil.uuidToBytes(entry.getRedirectUuid()));
            ps.setString(4, entry.getYggdrasilService());
            ps.setBoolean(5, entry.isWhitelist());
            ps.executeUpdate();
        }
    }

    /**
     * 更新一条现有的玩家数据
     *
     * @param entry 玩家数据
     * @throws SQLException 更新异常
     */
    public void updateUserEntry(User entry) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?",
                     USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_current_name(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_redirect_uuid(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_yggdrasil_service(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_whitelist(),
                     sqlManager.getCore().getSetting().getDatabase_user_data_table_field_online_uuid()
             ))) {
            ps.setString(1, entry.getCurrentName());
            ps.setBytes(2, ValueUtil.uuidToBytes(entry.getRedirectUuid()));
            ps.setString(3, entry.getYggdrasilService());
            ps.setBoolean(4, entry.isWhitelist());
            ps.setBytes(5, ValueUtil.uuidToBytes(entry.getOnlineUuid()));
            ps.executeUpdate();
        }
    }

    /**
     * 移除一条玩家数据
     *
     * @param entry 玩家数据实例
     * @return 是否移除成功
     * @throws SQLException 移除异常
     */
    public boolean deleteUserEntry(User entry) throws SQLException {
        return deleteUserEntry(entry.getOnlineUuid());
    }

    /**
     * 移除一条玩家数据
     *
     * @param uuid 玩家在线 uuid
     * @return 是否移除成功
     * @throws SQLException 移除异常
     */
    public boolean deleteUserEntry(UUID uuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("DELETE FROM %s WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_user_data_table_field_online_uuid()
        ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(uuid));
            return ps.executeUpdate() != 0;
        }
    }
}
