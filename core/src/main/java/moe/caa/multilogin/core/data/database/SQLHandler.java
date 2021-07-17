package moe.caa.multilogin.core.data.database;

import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.data.database.handler.UserDataHandler;
import moe.caa.multilogin.core.data.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.data.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.impl.ISQLConnectionPool;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.core.util.YamlConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLHandler {
    public static final String USER_DATA_TABLE_NAME = "user_data";
    public static final String ONLINE_UUID = "online_uuid";
    public static final String CURRENT_NAME = "current_name";
    public static final String REDIRECT_UUID = "redirect_name";
    public static final String YGGDRASIL_SERVICE = "yggdrasil_service";
    public static final String WHITELIST = "whitelist";
    public static final String CACHE_WHITELIST_TABLE_NAME = "whitelist";
    public static ISQLConnectionPool pool;

    public static void init() throws Exception {
        YamlConfig config = ValueUtil.getOrThrow(MultiCore.config.get("sql", YamlConfig.class), LanguageKeys.CONFIGURATION_KEY_ERROR.getMessage("sql"));
        String username = ValueUtil.getOrThrow(config.get("username", String.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("username"));
        String password = ValueUtil.getOrThrow(config.get("password", String.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("password"));
        String ip;
        int port;
        String database;
        String url;
        SQLBackend backend = ValueUtil.getOrThrow(config.get("backend", SQLBackend.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("backend"));
        if (backend == SQLBackend.MYSQL) {
            ip = ValueUtil.getOrThrow(config.get("ip", String.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("ip"));
            database = ValueUtil.getOrThrow(config.get("database", String.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("database"));
            port = ValueUtil.getOrThrow(config.get("port", Integer.class), LanguageKeys.CONFIGURATION_VALUE_ERROR.getMessage("port"));
            url = "jdbc:mysql://%s:%s/%s?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
            pool = new MysqlConnectionPool(String.format(url, ip, port, database), username, password);
        } else {
            url = "jdbc:h2:%s%s;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
            url = String.format(url, MultiCore.plugin.getDataFolder().getAbsolutePath(), "/multilogin");
            pool = new H2ConnectionPool(url, username, password);
        }

        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            UserDataHandler.init(s);
            CacheWhitelistDataHandler.init(s);
        }

        MultiLogger.log(LoggerLevel.INFO, LanguageKeys.DATABASE_CONNECTED.getMessage(backend.name()));
    }

    public static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    public static void close() {
        if (pool != null) {
            pool.close();
        }
    }
}
