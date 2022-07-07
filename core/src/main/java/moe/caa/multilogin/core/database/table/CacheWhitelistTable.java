package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;

public class CacheWhitelistTable {
    private static final String fieldCurrentUsername = "current_user_name";
    private static final String fieldOnlineUuid = "online_uuid";
    private static final String fieldYggdrasilId = "yggdrasil_id";

    private final String tableName;
    private final SQLManager sqlManager;

    public CacheWhitelistTable(String tableName, SQLManager sqlManager) {
        this.tableName = tableName;
        this.sqlManager = sqlManager;
    }

    public void init() throws SQLException {
        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s ( " +                  // tableName
                        "%s BINARY(16), " +                           // fieldOnlineUuid
                        "%s TINYINT, " +                              // fieldYggdrasilId
                        "%s VARCHAR(32))",                            // fieldCurrentUsername
                tableName, fieldOnlineUuid, fieldYggdrasilId, fieldCurrentUsername);
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 插入一条缓存白名单记录
     *
     * @param onlineUUID      用户的在线 UUID
     * @param yggdrasilId     用户的在线 UUID 提供的验证服务器 ID
     * @param currentUsername 用户的游戏内 UUID
     * @return 数据操作量
     */
    public int addCacheWhitelist(UUID onlineUUID, int yggdrasilId, String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}, {2}, {3}) " +
                                     "SELECT ?, ?, ? FROM DUAL WHERE NOT EXISTS(SELECT {1}, {2}, {3} FROM {0} WHERE " +
                                     "{1} = ? AND {2} = ? AND {3} = ?)",
                             tableName, fieldCurrentUsername,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.setString(4, currentUsername);
            preparedStatement.setBytes(5, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setInt(6, yggdrasilId);
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条缓存白名单记录
     *
     * @param onlineUUID      用户的在线 UUID
     * @param yggdrasilId     用户的在线 UUID 提供的验证服务器 ID
     * @param currentUsername 用户的游戏内 UUID
     * @return 数据操作量
     */
    public int removeCacheWhitelist(UUID onlineUUID, int yggdrasilId, String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format(
                             "DELETE FROM {0} WHERE " +
                                     "({2} IS NULL AND {3} IS NULL AND {1} IS NOT NULL AND {1} = ?) OR " +
                                     "({3} IS NULL AND {1} IS NULL AND {2} IS NOT NULL AND {2} = ?) OR " +
                                     "({1} IS NULL AND {2} IS NULL AND {3} IS NOT NULL AND {3} = ?) OR " +
                                     "({3} IS NULL AND {2} IS NOT NULL AND {1} IS NOT NULL AND {1} = ? AND {2} = ?) OR " +
                                     "({1} IS NULL AND {2} IS NOT NULL AND {3} IS NOT NULL AND {2} = ? AND {3} = ?) OR " +
                                     "({2} IS NULL AND {3} IS NOT NULL AND {1} IS NOT NULL AND {3} = ? AND {1} = ?) OR " +
                                     "({3} IS NOT NULL AND {2} IS NOT NULL AND {1} IS NOT NULL AND {1} = ? AND {2} = ? AND {3} = ?)"
                             , tableName,
                             fieldYggdrasilId, fieldOnlineUuid, fieldCurrentUsername
                     ))) {
            preparedStatement.setInt(1, yggdrasilId);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setString(3, currentUsername);

            preparedStatement.setInt(4, yggdrasilId);
            preparedStatement.setBytes(5, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setBytes(6, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setString(7, currentUsername);
            preparedStatement.setString(8, currentUsername);
            preparedStatement.setInt(9, yggdrasilId);

            preparedStatement.setInt(10, yggdrasilId);
            preparedStatement.setBytes(11, ValueUtil.uuidToBytes(onlineUUID));
            preparedStatement.setString(12, currentUsername);
            return preparedStatement.executeUpdate();
        }
    }
}
