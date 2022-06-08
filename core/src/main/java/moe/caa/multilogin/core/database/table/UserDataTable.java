package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * 玩家数据表
 */
public class UserDataTable {
    private static final String fieldOnlineUUID = "online_uuid";
    private static final String fieldYggdrasilId = "yggdrasil_id";
    private static final String fieldInGameProfileUuid = "in_game_profile_uuid";
    private final SQLManager sqlManager;
    private final String tableName;
    private final String inGameProfileTableName;

    public UserDataTable(SQLManager sqlManager, String tableName, String inGameProfileTableName) {
        this.sqlManager = sqlManager;
        this.tableName = tableName;
        this.inGameProfileTableName = inGameProfileTableName;
    }

    public void init() throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} TINYINT NOT NULL, " +
                        "{3} BINARY(16) DEFAULT NULL, " +
                        "PRIMARY KEY ( {1}, {2} ))"
                , tableName, fieldOnlineUUID, fieldYggdrasilId, fieldInGameProfileUuid, inGameProfileTableName, InGameProfileTable.fieldInGameUuid);
        // TODO: FOREIGN KEY ???
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
            statement.setInt(2, yggdrasilId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return ValueUtil.bytesToUuid(resultSet.getBytes(1));
                }
            }
        }
        return null;
    }

    /**
     * 从数据库中检索用户在线信息
     *
     * @param inGameUUID 用户游戏内 UUID
     * @return 检索到的用户在线信息
     */
    public List<Pair<UUID, Integer>> getOnlineInfo(UUID inGameUUID) throws SQLException {
        List<Pair<UUID, Integer>> result = new ArrayList<>();
        String sql = String.format(
                "SELECT %s, %s FROM %s WHERE %s = ?"
                , fieldOnlineUUID, fieldYggdrasilId, tableName, fieldInGameProfileUuid
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result.add(new Pair<>(
                            ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                            resultSet.getInt(2))
                    );
                }
            }
        }
        return Collections.unmodifiableList(result);
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
                if (resultSet.next()) {
                    result.add(resultSet.getInt(2));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 设置在线用户的游戏内 UUID
     *
     * @param onlineUUID  用户在线 UUID
     * @param yggdrasilId 用户在线 UUID 提供的验证服务器 ID
     * @param inGameUUID  新的用户在游戏内的 UUID
     * @return 数据操作量
     */
    public int setInGameUUID(UUID onlineUUID, int yggdrasilId, UUID inGameUUID) throws SQLException {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?"
                , tableName, fieldInGameProfileUuid, fieldOnlineUUID, fieldYggdrasilId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
            statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(3, yggdrasilId);
            return statement.executeUpdate();
        }
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
            statement.setInt(2, yggdrasilId);
            if (inGameUUID == null) {
                statement.setNull(3, Types.BINARY);
            } else {
                statement.setBytes(3, ValueUtil.uuidToBytes(onlineUUID));
            }
            return statement.executeUpdate();
        }
    }

    /**
     * 检查用户数据是否存在
     *
     * @param onlineUUID  用户在线 UUID
     * @param yggdrasilId 用户在线 UUID 提供的验证服务器 ID
     * @return 数据量
     */
    public int userDataExists(UUID onlineUUID, int yggdrasilId) throws SQLException {
        String sql = String.format(
                "SELECT COUNT(%s) FROM %s WHERE %s = ? AND %s = ?"
                , fieldOnlineUUID, tableName, fieldOnlineUUID, fieldYggdrasilId
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
            statement.setInt(2, yggdrasilId);
            return statement.executeUpdate();
        }
    }
}
