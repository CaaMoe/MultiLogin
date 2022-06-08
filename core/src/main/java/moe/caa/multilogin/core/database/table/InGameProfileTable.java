package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * 游戏内档案表
 */
public class InGameProfileTable {
    protected static final String fieldInGameUuid = "in_game_uuid";
    private static final String fieldCurrentUsername = "current_username";
    private static final String fieldWhitelist = "whitelist";

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
                        "{3} BOOL DEFAULT FALSE, " +
                        "CONSTRAINT IGPT_PR PRIMARY KEY ( {1} ), " +
                        "CONSTRAINT IGPT_UN UNIQUE ( {2} ))"
                , tableName, fieldInGameUuid, fieldCurrentUsername, fieldWhitelist);
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public String getCurrentUsername(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
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

    public boolean hasWhitelist(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
                , fieldWhitelist, tableName, fieldInGameUuid
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
        throw new NullPointerException();
    }

    public boolean hasWhitelist(String currentUsername) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
                , fieldWhitelist, tableName, fieldCurrentUsername
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, currentUsername);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }
        }
        throw new NullPointerException();
    }

    public int setCurrentUsername(UUID inGameUUID, String currentUsername) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?"
                , tableName, fieldCurrentUsername, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            statement.setString(2, currentUsername);
            return statement.executeUpdate();
        }
    }

    public int setWhitelist(UUID inGameUUID, boolean whitelist) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?"
                , tableName, fieldWhitelist, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            statement.setBoolean(2, whitelist);
            return statement.executeUpdate();
        }
    }
}
