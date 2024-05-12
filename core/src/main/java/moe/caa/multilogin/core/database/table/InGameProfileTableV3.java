package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

public class InGameProfileTableV3 {
    private static final String fieldInGameUuid = "in_game_uuid";
    private static final String fieldCurrentUsernameLowerCase = "current_username_lower_case";
    private static final String fieldCurrentUsernameOriginal = "current_username_original";
    private final String tableName;
    private final String tableNameV2;
    private final SQLManager sqlManager;

    public InGameProfileTableV3(SQLManager sqlManager, String tableName, String tableNameV2) {
        this.tableName = tableName;
        this.sqlManager = sqlManager;
        this.tableNameV2 = tableNameV2;
    }


    public void init(Connection connection) throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} VARCHAR(64) DEFAULT NULL, " +
                        "{3} VARCHAR(64) DEFAULT NULL, " +
                        "CONSTRAINT IGPT_V3_PR PRIMARY KEY ( {1} ), " +
                        "CONSTRAINT IGPT_V3_UN UNIQUE ( {2} ))"
                , tableName, fieldInGameUuid, fieldCurrentUsernameLowerCase, fieldCurrentUsernameOriginal);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            // 查新表有没有数据，没有的话就尝试一下数据升级
            try (
                    PreparedStatement prepareStatement = connection.prepareStatement("SELECT COUNT(0) FROM " + tableName);
                    ResultSet resultSet = prepareStatement.executeQuery();
            ) {
                resultSet.next();
                if (resultSet.getInt(1) != 0) {
                    // 新表里面有数据，不需要升级
                    return;
                }
            }
            try (
                    PreparedStatement statement = connection.prepareStatement("SELECT COUNT(0) FROM " + tableNameV2);
                    ResultSet resultSet = statement.executeQuery()
            ) {
                resultSet.next();
                if (resultSet.getInt(1) == 0) {
                    // 老表里面没有数据，不需要升级
                    return;
                }
            } catch (Exception ignored) {
                // 老表不存在，不需要进行升级
                return;
            }
        }

        LoggerProvider.getLogger().info("Updating in game profile data...");
        // 读老表
        List<Pair<byte[], String>> oldData = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT in_game_uuid, current_username FROM " + tableNameV2);
             ResultSet resultSet = statement.executeQuery();) {
            while (resultSet.next()) {
                oldData.add(new Pair<>(resultSet.getBytes(1), resultSet.getString(2)));
            }
        }
        for (Pair<byte[], String> datum : oldData) {
            try (PreparedStatement statement = connection.prepareStatement(
                    String.format(
                            "INSERT INTO %s (%s, %s) VALUES (?, ?)", tableName, fieldInGameUuid, fieldCurrentUsernameLowerCase
                    )
            )) {
                statement.setBytes(1, datum.getValue1());
                statement.setString(2, Optional.ofNullable(datum.getValue2()).map(String::toLowerCase).orElse(null));
                statement.executeUpdate();
            }
        }
        LoggerProvider.getLogger().info("Updated in game profile data, total " + oldData.size() + ".");
    }

    public Pair<UUID, String> get(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
                , fieldCurrentUsernameOriginal, tableName, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String string = resultSet.getString(1);
                    return new Pair<>(inGameUUID, string);
                }
            }
        }
        return null;
    }

    /**
     * 获得游戏内 UUID
     *
     * @param currentUsername 用户名
     * @return 游戏内 UUID
     */
    public UUID getInGameUUIDIgnoreCase(String currentUsername) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE LOWER(%s) = ? LIMIT 1"
                , fieldInGameUuid, tableName, fieldCurrentUsernameLowerCase
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
     * 获得游戏内名字
     *
     * @param inGameUUID 游戏内 UUID
     */
    public String getUsername(UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? LIMIT 1"
                , fieldCurrentUsernameOriginal, tableName, fieldInGameUuid
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
     * 更新用户名
     *
     * @param inGameUUID      游戏内 UUID
     * @param currentUsername 新的名字
     * @throws SQLException
     */
    public void updateUsername(UUID inGameUUID, String currentUsername) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ?, %s = ? WHERE %s = ?"
                , tableName, fieldCurrentUsernameLowerCase, fieldCurrentUsernameOriginal, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, currentUsername.toLowerCase(Locale.ROOT));
            statement.setString(2, currentUsername);
            statement.setBytes(3, ValueUtil.uuidToBytes(inGameUUID));
            statement.executeUpdate();
        }
    }

    /**
     * 插入一条新的数据
     *
     * @param inGameUUID 游戏内 UUID
     */
    public void insertNewData(UUID inGameUUID, String currentUsername) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)"
                , tableName, fieldInGameUuid, fieldCurrentUsernameLowerCase, fieldCurrentUsernameOriginal
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            connection.setAutoCommit(false);
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            statement.setString(2, currentUsername.toLowerCase());
            statement.setString(3, currentUsername);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public boolean remove(UUID uuid) throws SQLException {
        String sql = String.format(
                "DELETE FROM %s WHERE %s = ?"
                , tableName, fieldInGameUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(uuid));
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * 擦除用户名使用记录
     *
     * @param currentUsername 用户名
     */
    public int eraseUsername(String currentUsername) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ?, %s = ? WHERE LOWER(%s) = ?"
                , tableName, fieldCurrentUsernameLowerCase, fieldCurrentUsernameOriginal, fieldCurrentUsernameLowerCase
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, null);
            statement.setString(2, null);
            statement.setString(3, currentUsername.toLowerCase(Locale.ROOT));
            return statement.executeUpdate();
        }
    }

    public int eraseAllUsername() throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ?, %s = ?"
                , tableName, fieldCurrentUsernameLowerCase, fieldCurrentUsernameOriginal
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, null);
            statement.setString(2, null);
            return statement.executeUpdate();
        }
    }
}
