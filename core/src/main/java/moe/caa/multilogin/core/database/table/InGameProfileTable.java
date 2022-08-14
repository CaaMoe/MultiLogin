package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * 游戏内档案表
 */

// current_username 写入和读取需要使用小写！
public class InGameProfileTable {
    protected static final String fieldInGameUuid = "in_game_uuid";
    private static final String fieldCurrentUsername = "current_username";

    private final String tableName;
    private final SQLManager sqlManager;

    public InGameProfileTable(SQLManager sqlManager, String tableName) {
        this.tableName = tableName;
        this.sqlManager = sqlManager;
    }

    public void init() throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} VARCHAR(64) DEFAULT NULL, " +
                        "CONSTRAINT IGPT_PR PRIMARY KEY ( {1} ), " +
                        "CONSTRAINT IGPT_UN UNIQUE ( {2} ))"
                , tableName, fieldInGameUuid, fieldCurrentUsername);
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 查询数据是否存在
     *
     * @param inGameUUID 游戏内 UUID
     * @return 是否存在数据
     */
    public boolean dataExists(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT 1 FROM %s WHERE %s = ? LIMIT 1"
                , tableName, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * 获得游戏内 UUID
     *
     * @param currentUsername 用户名
     * @return 游戏内 UUID
     */
    public UUID getInGameUUID(String currentUsername) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE LOWER(%s) = ? LIMIT 1"
                , fieldInGameUuid, tableName, fieldCurrentUsername
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, currentUsername.toLowerCase(Locale.ROOT));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return ValueUtil.bytesToUuid(resultSet.getBytes(1));
                }
            }
        }
        return null;
    }

    /**
     * 获得游戏内名字
     *
     * @param inGameUUID 游戏内 UUID
     */
    public String getUsername(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE LOWER(%s) = ? LIMIT 1"
                , fieldCurrentUsername, tableName, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
        }
        return null;
    }

    /**
     * 插入一条新的数据
     *
     * @param inGameUUID 游戏内 UUID
     */
    public void insertNewData(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s) VALUES (?)"
                , tableName, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            statement.executeUpdate();
        }
    }

    /**
     * 更新用户名
     *
     * @param inGameUUID      游戏内 UUID
     * @param currentUsername 新的名字
     * @throws SQLException
     */
    public void updateUsername(UUID inGameUUID, String currentUsername) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?"
                , tableName, fieldCurrentUsername, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, currentUsername.toLowerCase(Locale.ROOT));
            statement.setBytes(2, ValueUtil.uuidToBytes(inGameUUID));
            statement.executeUpdate();
        }
    }

    /**
     * 擦除用户名使用记录
     *
     * @param currentUsername 用户名
     */
    public int eraseUsername(String currentUsername) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?"
                , tableName, fieldCurrentUsername, fieldCurrentUsername
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, null);
            statement.setString(2, currentUsername.toLowerCase(Locale.ROOT));
            return statement.executeUpdate();
        }
    }
}
