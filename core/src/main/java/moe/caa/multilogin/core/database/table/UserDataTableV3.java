package moe.caa.multilogin.core.database.table;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * 玩家数据表
 */
public class UserDataTableV3 {
    private static final String fieldOnlineUUID = "online_uuid";
    private static final String fieldOnlineName = "online_name";
    private static final String fieldServiceId = "service_id";
    private static final String fieldInGameProfileUuid = "in_game_profile_uuid";
    private static final String fieldWhitelist = "whitelist";
    private final SQLManager sqlManager;
    private final String tableName;
    private final String tableNameV2;

    public UserDataTableV3(SQLManager sqlManager, String tableName, String tableNameV2) {
        this.sqlManager = sqlManager;
        this.tableName = tableName;
        this.tableNameV2 = tableNameV2;
    }

    public void init(Connection connection) throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} INTEGER NOT NULL, " +
                        "{3} VARCHAR(64) DEFAULT NULL, " +
                        "{4} BINARY(16) DEFAULT NULL, " +
                        "{5} BOOL DEFAULT FALSE, " +
                        "PRIMARY KEY ( {1}, {2} ))"
                , tableName, fieldOnlineUUID, fieldServiceId, fieldOnlineName, fieldInGameProfileUuid, fieldWhitelist);
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


        LoggerProvider.getLogger().info("Updating user data...");
        @AllArgsConstructor
        class V2Entry {
            private final byte[] onlineUUID;
            private final int serviceId;
            private final byte[] inGameProfileUUID;
            private final boolean whitelist;
        }
        // 读老表
        List<V2Entry> oldData = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT online_uuid, yggdrasil_id, in_game_profile_uuid, whitelist FROM " + tableNameV2);
             ResultSet resultSet = statement.executeQuery();) {
            while (resultSet.next()) {
                oldData.add(new V2Entry(resultSet.getBytes(1),
                        resultSet.getBytes(2)[0],
                        resultSet.getBytes(3),
                        resultSet.getBoolean(4))
                );
            }
        }
        for (V2Entry datum : oldData) {
            try (PreparedStatement statement = connection.prepareStatement(
                    String.format(
                            "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)", tableName, fieldOnlineUUID, fieldServiceId, fieldInGameProfileUuid, fieldWhitelist
                    )
            )) {
                statement.setBytes(1, datum.onlineUUID);
                statement.setInt(2, datum.serviceId);
                statement.setBytes(3, datum.inGameProfileUUID);
                statement.setBoolean(4, datum.whitelist);
                statement.executeUpdate();
            }
        }
        LoggerProvider.getLogger().info("Updated user data, total " + oldData.size() + ".");
    }

    /**
     * 从数据库中检索用户游戏内 UUID
     *
     * @param onlineUUID  用户在线 UUID
     * @param serviceId 用户在线 UUID 提供的验证服务器 ID
     * @return 检索到的用户游戏内 UUID
     */
    public UUID getInGameUUID(UUID onlineUUID, int serviceId) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , fieldInGameProfileUuid, tableName, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(2, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] bytes = resultSet.getBytes(1);
                    if (bytes == null) return null;
                    return ValueUtil.bytesToUuid(bytes);
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
    public Set<Integer> getOnlineServiceIds(UUID inGameUUID) throws SQLException {
        Set<Integer> result = new HashSet<>();
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?"
                , fieldServiceId, tableName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(((int) resultSet.getInt(1)));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 返回档案集合
     *
     * @param inGameUUID 游戏内 UUID
     */
    public Set<Pair<UUID, Integer>> getOnlineProfiles(UUID inGameUUID) throws SQLException {
        Set<Pair<UUID, Integer>> result = new HashSet<>();
        String sql = String.format(
                "SELECT %s, %s FROM %s WHERE %s = ?"
                , fieldOnlineUUID, fieldServiceId, tableName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(new Pair<>(
                            ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                            resultSet.getInt(2)
                    ));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 设置游戏内 UUID
     *
     * @param onlineUUID    在线 UUID
     * @param serviceId     service ID
     * @param newInGameUUID 新的游戏内 UUID
     */
    public int setInGameUUID(UUID onlineUUID, int serviceId, UUID newInGameUUID) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1"
                , tableName, fieldInGameProfileUuid, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(newInGameUUID));
            statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(3, serviceId);
            return statement.executeUpdate();
        }
    }

    /**
     * 查询数据是否存在
     *
     * @param onlineUUID  在线UUID
     * @param serviceId service Id
     */
    public boolean dataExists(UUID onlineUUID, int serviceId) throws SQLException {
        String sql = String.format(
                "SELECT 1 FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , tableName, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(2, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * 插入一条用户数据
     *
     * @param onlineUUID  用户在线 UUID
     * @param serviceId 用户在线 UUID 提供的验证服务器 ID
     * @param inGameUUID  新的用户在游戏内的 UUID
     * @return 数据操作量
     */
    public int insertNewData(UUID onlineUUID, int serviceId, String onlineName, UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) "
                , tableName, fieldOnlineUUID, fieldServiceId, fieldOnlineName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(2, serviceId);
            statement.setString(3, onlineName);
            if (inGameUUID == null) {
                statement.setNull(4, Types.BINARY);
            } else {
                statement.setBytes(4, ValueUtil.uuidToBytes(inGameUUID));
            }
            return statement.executeUpdate();
        }
    }

    /**
     * 设置白名单
     *
     * @param onlineUUID  在线 UUID
     * @param serviceId service Id
     * @param whitelist   新的白名单
     */
    public void setWhitelist(UUID onlineUUID, int serviceId, boolean whitelist) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1"
                , tableName, fieldWhitelist, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, whitelist);
            statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(3, serviceId);
            statement.executeUpdate();
        }
    }

    /**
     * 查询白名单
     */
    public boolean hasWhitelist(UUID onlineUUID, int serviceId) throws SQLException {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , fieldWhitelist, tableName, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(2, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }
        }
        return false;
    }

    /**
     * 查询白名单
     */
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

    /**
     * 设置白名单
     */
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

    public void setOnlineName(UUID onlineUUID, int serviceId, String onlineName) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1"
                , tableName, fieldOnlineName, fieldOnlineUUID, fieldServiceId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, onlineName);
            statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(3, serviceId);
            statement.executeUpdate();
        }
    }
}