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
