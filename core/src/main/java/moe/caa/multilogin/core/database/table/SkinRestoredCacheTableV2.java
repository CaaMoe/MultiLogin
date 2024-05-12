package moe.caa.multilogin.core.database.table;

import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * 皮肤修复缓存表
 */
public class SkinRestoredCacheTableV2 {
    private static final String fieldCurrentSkinUrlSha256 = "current_skin_url_sha256";
    private static final String fieldCurrentSkinModel = "current_skin_model";
    private static final String fieldRestorerValue = "restorer_value";
    private static final String fieldRestorerSignature = "restorer_signature";
    private final SQLManager sqlManager;
    private final String tableName;

    public SkinRestoredCacheTableV2(SQLManager sqlManager, String tableName) {
        this.sqlManager = sqlManager;
        this.tableName = tableName;
    }

    public void init(Connection connection) throws SQLException {
        String sql = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(32) NOT NULL, " +
                        "{2} VARCHAR(16) NOT NULL, " +
                        "{3} LONGTEXT NOT NULL, " +
                        "{4} LONGTEXT NOT NULL, " +
                        "PRIMARY KEY ( {1}, {2} ))"
                , tableName, fieldCurrentSkinUrlSha256, fieldCurrentSkinModel, fieldRestorerValue, fieldRestorerSignature);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 获得缓存的数据对象
     *
     * @param urlSha256 皮肤 URL
     * @param model     皮肤模型
     * @return 缓存的对象
     */
    public Pair<String, String> getCacheRestored(byte[] urlSha256, String model) throws SQLException {
        String sql = String.format(
                "SELECT %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1"
                , fieldRestorerValue, fieldRestorerSignature, tableName, fieldCurrentSkinUrlSha256, fieldCurrentSkinModel
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, urlSha256);
            statement.setString(2, model);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Pair<>(resultSet.getString(1), resultSet.getString(2));
                }
            }
        }
        return null;
    }

    /**
     * 插入新的缓存对象
     *
     * @param urlSha256 皮肤 URL
     * @param model     皮肤模型
     * @param value     值
     * @param signature 签名
     */
    public void insertNew(byte[] urlSha256, String model, String value, String signature) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) "
                , tableName, fieldCurrentSkinUrlSha256, fieldCurrentSkinModel, fieldRestorerValue, fieldRestorerSignature
        );
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBytes(1, urlSha256);
            statement.setString(2, model);
            statement.setString(3, value);
            statement.setString(4, signature);
            statement.executeUpdate();
        }
    }
}
