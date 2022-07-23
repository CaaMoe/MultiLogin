package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 玩家数据表
 */
public class UserDataTable {
    private static final String fieldOnlineUUID = "online_uuid";
    private static final String fieldYggdrasilId = "yggdrasil_id";
    private static final String fieldInGameProfileUuid = "in_game_profile_uuid";
    private static final String fieldWhitelist = "whitelist";
    private final SQLManager sqlManager;
    private final String tableName;

    public UserDataTable(SQLManager sqlManager, String tableName) {
        this.sqlManager = sqlManager;
        this.tableName = tableName;
    }

    public void init() throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} BINARY(1) NOT NULL, " +
                        "{3} BINARY(16) NOT NULL, " +
                        "{4} BOOL DEFAULT FALSE, " +
                        "PRIMARY KEY ( {1}, {2} ))"
                , tableName, fieldOnlineUUID, fieldYggdrasilId, fieldInGameProfileUuid, fieldWhitelist);
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 从数据库中检索用户游戏内 UUID
     *
     * @param onlineUUID  用户在线 UUID
     * @param yggdrasilId 用户在线 UUID 提供的验证服务器 ID
     * @return 检索到的用户游戏内 UUID
     */
    public UUID getInGameUUID(UUID onlineUUID, int yggdrasilId) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , fieldInGameProfileUuid, tableName, fieldOnlineUUID, fieldYggdrasilId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setBytes(2, new byte[]{(byte) yggdrasilId});
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return ValueUtil.bytesToUuid(resultSet.getBytes(1));
                }
            }
        }
        return null;
    }

    /**
     * 从数据库中检索用户登录时所用的账户验证服务器 ID
     *
     * @param inGameUUID 用户游戏内 UUID
     * @return 检索到的用户在线信息
     */
    public Set<Integer> getOnlineYggdrasilIds(UUID inGameUUID) throws SQLException {
        Set<Integer> result = new HashSet<>();
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?"
                , fieldYggdrasilId, tableName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(((int) resultSet.getBytes(1)[0]));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 插入一条用户数据
     *
     * @param onlineUUID  用户在线 UUID
     * @param yggdrasilId 用户在线 UUID 提供的验证服务器 ID
     * @param inGameUUID  新的用户在游戏内的 UUID
     * @return 数据操作量
     */
    public int insertNewData(UUID onlineUUID, int yggdrasilId, UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?) "
                , tableName, fieldOnlineUUID, fieldYggdrasilId, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setBytes(2, new byte[]{(byte) yggdrasilId});
            statement.setBytes(3, ValueUtil.uuidToBytes(inGameUUID));
            return statement.executeUpdate();
        }
    }

    public void setWhitelist(UUID onlineUUID, int yggdrasilId, boolean whitelist) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1"
                , tableName, fieldWhitelist, fieldOnlineUUID, fieldYggdrasilId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, whitelist);
            statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            statement.setBytes(3, new byte[]{(byte) yggdrasilId});
            statement.executeUpdate();
        }
    }

    public boolean hasWhitelist(UUID onlineUUID, int yggdrasilId) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , fieldWhitelist, tableName, fieldOnlineUUID, fieldYggdrasilId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setBytes(2, new byte[]{(byte) yggdrasilId});
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }
        }
        return false;
    }

    public boolean hasWhitelist(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
                , fieldWhitelist, tableName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }
        }
        return false;
    }

    public void setWhitelist(UUID inGameUUID, boolean whitelist) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?LIMIT 1"
                , tableName, fieldWhitelist, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, whitelist);
            statement.setBytes(2, ValueUtil.uuidToBytes(inGameUUID));
            statement.executeUpdate();
        }
    }
}