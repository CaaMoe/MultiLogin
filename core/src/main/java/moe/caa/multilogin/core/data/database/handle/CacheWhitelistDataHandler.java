package moe.caa.multilogin.core.data.database.handle;

import moe.caa.multilogin.core.data.database.IDataHandler;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static moe.caa.multilogin.core.data.database.SQLManager.CACHE_WHITELIST_TABLE_NAME;


/**
 * 擦车白名单管理类
 */
public class CacheWhitelistDataHandler implements IDataHandler {
    private final SQLManager sqlManager;

    /**
     * 构建这个擦车白名单管理类
     *
     * @param sqlManager 数据库管理类
     */
    public CacheWhitelistDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    /**
     * 建表操作
     *
     * @param statement 链接
     * @throws SQLException 创表异常
     */
    @Override
    public void createIfNotExists(Statement statement) throws SQLException {
        statement.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS " + CACHE_WHITELIST_TABLE_NAME + "( " +
                sqlManager.getCore().getSetting().getDatabase_cache_whitelist_table_field_sign() + " VARCHAR(100) PRIMARY KEY NOT NULL)");
    }

    /**
     * 获得所有缓存白名单列表
     *
     * @return 缓存白名单列表
     * @throws SQLException 查询异常
     */
    public List<String> getAllCacheWhitelist() throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s",
                CACHE_WHITELIST_TABLE_NAME
        ))) {
            ResultSet resultSet = ps.executeQuery();
            List<String> ret = new ArrayList<>();
            while (resultSet.next()) {
                ret.add(resultSet.getString(1));
            }
            return ret;
        }
    }

    /**
     * 移除擦车白名单操作
     *
     * @param nameOrUuid 项目
     * @return 是否移除成功
     * @throws SQLException 移除时异常
     */
    public boolean removeCacheWhitelist(String nameOrUuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("DELETE FROM %s WHERE %s = ?",
                CACHE_WHITELIST_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_cache_whitelist_table_field_sign()
        ))) {
            ps.setString(1, nameOrUuid);
            return ps.executeUpdate() != 0;
        }
    }

    /**
     * 添加擦车白名单操作
     *
     * @param nameOrUuid 项目
     * @return 是否添加成功
     * @throws SQLException 添加时异常
     */
    public boolean addCacheWhitelist(String nameOrUuid) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO %s (%s) VALUES(?)",
                CACHE_WHITELIST_TABLE_NAME, sqlManager.getCore().getSetting().getDatabase_cache_whitelist_table_field_sign()
        ))) {
            ps.setString(1, nameOrUuid);
            return ps.executeUpdate() != 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Duplicate whitelist.", e);
            return false;
        }
    }
}
