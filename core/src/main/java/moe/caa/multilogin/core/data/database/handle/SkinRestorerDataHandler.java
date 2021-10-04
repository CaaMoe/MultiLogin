package moe.caa.multilogin.core.data.database.handle;

import moe.caa.multilogin.core.data.database.IDataHandler;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.skinrestorer.RestorerEntry;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.ValueUtil;

import java.sql.*;
import java.util.UUID;

import static moe.caa.multilogin.core.data.database.SQLManager.SKIN_RESTORER_TABLE_NAME;

/**
 * 皮肤数据管理类
 */
public class SkinRestorerDataHandler implements IDataHandler {
    private final SQLManager sqlManager;

    /**
     * 构建这个皮肤数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public SkinRestorerDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    @Override
    public void createIfNotExists(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + SKIN_RESTORER_TABLE_NAME + "( " +
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_online_uuid() + " BINARY(16) PRIMARY KEY NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_current_skin_url() + " LONGTEXT NOT NULL, " +
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_restorer_data() + " LONGTEXT NOT NULL)");
    }

    /**
     * 合成皮肤数据对象
     *
     * @param resultSet 数据库检索结果
     * @return 皮肤数据对象
     * @throws SQLException 读取异常
     */
    private RestorerEntry getRestorer(ResultSet resultSet) throws SQLException {
        return new RestorerEntry(
                ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                resultSet.getString(2),
                resultSet.getString(3)
        );
    }

    /**
     * 通过主键 onlineUuid 获取皮肤数据对象
     *
     * @param uuid 在线 uuid
     * @return 皮肤数据对象
     * @throws SQLException 读取异常
     */
    public RestorerEntry getRestorerEntryByOnlineUuid(UUID uuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? limit 1",
                SKIN_RESTORER_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_online_uuid()
        ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(uuid));
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return getRestorer(resultSet);
            }
            return null;
        }
    }


    /**
     * 写入一条新的皮肤数据
     *
     * @param entry 皮肤数据
     * @throws SQLException 写入异常
     */
    public void writeNewRestorerEntry(RestorerEntry entry) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)",
                SKIN_RESTORER_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_online_uuid(),
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_current_skin_url(),
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_restorer_data()
        ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(entry.getOnline_uuid()));
            ps.setString(2, entry.getCurrent_skin_url());
            ps.setString(3, entry.getRestorer_data());
            ps.executeUpdate();
        }
    }

    /**
     * 更新一条现有的皮肤数据
     *
     * @param entry 皮肤数据
     * @throws SQLException 更新异常
     */
    public void updateRestorerEntry(RestorerEntry entry) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
                SKIN_RESTORER_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_current_skin_url(),
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_restorer_data(),
                sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_online_uuid()
        ))) {
            ps.setString(1, entry.getCurrent_skin_url());
            ps.setString(2, entry.getRestorer_data());
            ps.setBytes(3, ValueUtil.uuidToBytes(entry.getOnline_uuid()));
            ps.executeUpdate();
        }
    }

    /**
     * 移除一条皮肤数据
     *
     * @param entry 皮肤数据实例
     * @return 是否移除成功
     * @throws SQLException 移除异常
     */
    public boolean deleteRestorerEntry(User entry) throws SQLException {
        return deleteRestorerEntry(entry.getOnlineUuid());
    }

    /**
     * 移除一条皮肤数据
     *
     * @param uuid 玩家在线 uuid
     * @return 是否移除成功
     * @throws SQLException 移除异常
     */
    public boolean deleteRestorerEntry(UUID uuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("DELETE FROM %s WHERE %s = ?",
                SKIN_RESTORER_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_skin_restorer_table_field_online_uuid()
        ))) {
            ps.setBytes(1, ValueUtil.uuidToBytes(uuid));
            return ps.executeUpdate() != 0;
        }
    }
}
