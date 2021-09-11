package moe.caa.multilogin.core.data.database.handle;

import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.*;
import java.util.*;

import static moe.caa.multilogin.core.data.database.SQLManager.*;

/**
 * 玩家数据管理类
 */
public class UserDataHandler {

    private final SQLManager sqlManager;

    /**
     * 构建这个玩家数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public UserDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    /**
     * 建表操作
     *
     * @param statement 链接
     * @throws SQLException 创表异常
     */
    public void createIfNotExists(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + USER_DATA_TABLE_NAME + "( " +
                ONLINE_UUID + " BINARY(16) PRIMARY KEY NOT NULL, " +
                CURRENT_NAME + " VARCHAR(100) NOT NULL, " +
                REDIRECT_UUID + " BINARY(16) NOT NULL, " +
                YGGDRASIL_SERVICE + " VARCHAR(100) NOT NULL, " +
                WHITELIST + " BOOL NOT NULL)");
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
                ValueUtil.byteToUuid(resultSet.getBytes(1)),
                resultSet.getString(2),
                ValueUtil.byteToUuid(resultSet.getBytes(3)),
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
                USER_DATA_TABLE_NAME, ONLINE_UUID
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(uuid));
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return getUser(resultSet);
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
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                USER_DATA_TABLE_NAME, REDIRECT_UUID
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(uuid));
            return getUsers(ps.executeQuery());
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
                USER_DATA_TABLE_NAME, CURRENT_NAME
        ))) {
            ps.setString(1, name);
            return getUsers(ps.executeQuery());
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
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT %s FROM %s WHERE %s = ?",
                YGGDRASIL_SERVICE, USER_DATA_TABLE_NAME, CURRENT_NAME
        ))) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            Set<YggdrasilService> ret = new HashSet<>();
            while (resultSet.next()) {
                YggdrasilService yggdrasilService = sqlManager.getCore().getYggdrasilServicesHandler().getYggdrasilService(resultSet.getString(1));
                if (yggdrasilService != null) ret.add(yggdrasilService);
            }
            return ret;
        }
    }

    /**
     * 写入一条新的玩家数据
     *
     * @param entry 玩家数据
     * @throws SQLException 写入异常
     */
    public void writeNewUserEntry(User entry) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                USER_DATA_TABLE_NAME, ONLINE_UUID, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST
        ))) {
            ps.setBytes(1, ValueUtil.uuidToByte(entry.getOnlineUuid()));
            ps.setString(2, entry.getCurrentName());
            ps.setBytes(3, ValueUtil.uuidToByte(entry.getRedirectUuid()));
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
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? limit 1",
                USER_DATA_TABLE_NAME, CURRENT_NAME, REDIRECT_UUID, YGGDRASIL_SERVICE, WHITELIST, ONLINE_UUID
        ))) {
            ps.setString(1, entry.getCurrentName());
            ps.setBytes(2, ValueUtil.uuidToByte(entry.getRedirectUuid()));
            ps.setString(3, entry.getYggdrasilService());
            ps.setBoolean(4, entry.isWhitelist());
            ps.setBytes(5, ValueUtil.uuidToByte(entry.getOnlineUuid()));
            ps.executeUpdate();
        }
    }
}
