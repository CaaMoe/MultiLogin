package moe.caa.multilogin.core.data.database;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.data.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.data.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.data.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.exception.UnsupportedDatabaseException;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlConfig;

/**
 * 数据库管理类
 */
@NoArgsConstructor
public class SQLManager {
    public static final String ONLINE_UUID = "online_uuid";
    public static final String CURRENT_NAME = "current_name";
    public static final String REDIRECT_UUID = "redirect_name";
    public static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    public static final String WHITELIST = "whitelist";
    public static String USER_DATA_TABLE_NAME = "user_data";
    public static String CACHE_WHITELIST_TABLE_NAME = "whitelist";

    @Getter
    private ISQLConnectionPool pool;

    /**
     * 初始化和链接数据库
     *
     * @param core   插件核心
     * @param config 数据库配置文件节点部分
     * @return 是否链接成功
     */
    public boolean init(MultiCore core, YamlConfig config) {
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
                var url = config.get("mysqlUrl", String.class, "");
                if (ValueUtil.isEmpty(url))
                    url = "jdbc:mysql://{ip}:{port}/{database}?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
                url = ValueUtil.format(url, new FormatContent(
                        FormatContent.FormatEntry.builder().name("ip").content(ip).build(),
                        FormatContent.FormatEntry.builder().name("port").content(port).build(),
                        FormatContent.FormatEntry.builder().name("database").content(database).build()
                ));
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Linking database(%s): %s", backend.name(), url));
                pool = new MysqlConnectionPool(url, username, password);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("Linked to database(%s).", backend.name()));
            } else if (backend == SQLBackendEnum.H2) {
                var url = config.get("h2Url", String.class, "");
                if (ValueUtil.isEmpty(url)) url = "jdbc:h2:{file_path};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
                url = ValueUtil.format(url, new FormatContent(
                        FormatContent.FormatEntry.builder().name("file_path").content(core.getPlugin().getDataFolder().getAbsolutePath() + "/multilogin").build()
                ));
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Linking database(%s): %s", backend.name(), url));
                pool = new H2ConnectionPool(url, username, password);
                MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("Linked to database(%s).", backend.name()));
            } else {
                pool = null;
                throw new UnsupportedDatabaseException();
            }
            return true;
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "无法链接到数据库", e);
        }
        return false;
    }

    public void close() {
        try {
            if (pool != null) pool.close();
        } catch (Throwable e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "关闭数据库时出现异常", e);
        }
    }
}
