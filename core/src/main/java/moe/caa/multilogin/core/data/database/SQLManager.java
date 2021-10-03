package moe.caa.multilogin.core.data.database;

import lombok.Getter;
import lombok.var;
import moe.caa.multilogin.core.data.database.handle.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.database.handle.UserDataHandler;
import moe.caa.multilogin.core.data.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.data.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.data.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.exception.UnsupportedDatabaseException;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlReader;

import java.sql.SQLException;

/**
 * 数据库管理类
 */
@Getter
public class SQLManager {
    public static final String ONLINE_UUID = "online_uuid";
    public static final String CURRENT_NAME = "current_name";
    public static final String REDIRECT_UUID = "redirect_name";
    public static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    public static final String WHITELIST = "whitelist";
    public static String USER_DATA_TABLE_NAME = "user_data";
    public static String CACHE_WHITELIST_TABLE_NAME = "whitelist";
    private final MultiCore core;
    private final CacheWhitelistDataHandler cacheWhitelistDataHandler = new CacheWhitelistDataHandler(this);
    private final UserDataHandler userDataHandler = new UserDataHandler(this);
    private ISQLConnectionPool pool;

    public SQLManager(MultiCore core) {
        this.core = core;
    }

    /**
     * 初始化和链接数据库
     *
     * @param config 数据库配置文件节点部分
     * @return 是否链接成功
     */
    public boolean init(YamlReader config) {
        try {
            var backend = config.get("backend", SQLBackendEnum.class, SQLBackendEnum.H2);
            var prefix = config.get("prefix", String.class, "multilogin");
            if (prefix.trim().length() != 0) {
                USER_DATA_TABLE_NAME = prefix + "_" + USER_DATA_TABLE_NAME;
                CACHE_WHITELIST_TABLE_NAME = prefix + "_" + CACHE_WHITELIST_TABLE_NAME;
            }
            var username = config.get("username", String.class, "root");
            var password = config.get("password", String.class, "12345");
            if (backend == SQLBackendEnum.MYSQL) {
                var ip = config.get("ip", String.class, "127.0.0.1");
                var port = config.get("port", Number.class, 3306).intValue();
                var database = config.get("database", String.class, "multilogin");
                var url = "jdbc:mysql://{ip}:{port}/{database}?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
                url = ValueUtil.format(url, FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("ip").content(ip).build(),
                        FormatContent.FormatEntry.builder().name("port").content(port).build(),
                        FormatContent.FormatEntry.builder().name("database").content(database).build()
                ));
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Linking database(%s): %s", backend.name(), url));
                pool = new MysqlConnectionPool(url, username, password);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("Linked to database(%s).", backend.name()));
            } else if (backend == SQLBackendEnum.H2) {
                var url = "jdbc:h2:{file_path};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
                url = ValueUtil.format(url, FormatContent.createContent(
                        FormatContent.FormatEntry.builder().name("file_path").content(core.getPlugin().getDataFolder().getAbsolutePath() + "/multilogin").build()
                ));
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Linking database(%s): %s", backend.name(), url));
                pool = new H2ConnectionPool(url, username, password);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("Linked to database(%s).", backend.name()));
            } else {
                pool = null;
                throw new UnsupportedDatabaseException();
            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "无法链接到数据库", e);
            return false;
        }
        try {
            loadBase();
        } catch (SQLException e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "初始化数据库时发生异常", e);
            return false;
        }
        return true;
    }

    public void loadBase() throws SQLException {
        userDataHandler.createIfNotExists(pool.getConnection().createStatement());
        cacheWhitelistDataHandler.createIfNotExists(pool.getConnection().createStatement());
    }

    /**
     * 关闭数据库
     */
    public void close() {
        try {
            if (pool != null) pool.close();
        } catch (Throwable e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "关闭数据库时出现异常", e);
        }
    }
}
