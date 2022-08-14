package moe.caa.multilogin.core.database;

import lombok.Getter;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.SqlConfig;
import moe.caa.multilogin.core.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.database.table.InGameProfileTable;
import moe.caa.multilogin.core.database.table.SkinRestoredCacheTable;
import moe.caa.multilogin.core.database.table.UserDataTable;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;

/**
 * 数据库管理程序
 */
public class SQLManager {
    @Getter
    private final MultiCore core;
    @Getter
    private ISQLConnectionPool pool;
    @Getter
    private InGameProfileTable inGameProfileTable;
    @Getter
    private UserDataTable userDataTable;
    @Getter
    private SkinRestoredCacheTable skinRestoredCacheTable;


    public SQLManager(MultiCore core) {
        this.core = core;
    }

    public void init() throws SQLException, ClassNotFoundException {
        SqlConfig sqlConfig = core.getPluginConfig().getSqlConfig();
        if (sqlConfig.getBackend() == SqlConfig.SqlBackend.MYSQL) {
            pool = new MysqlConnectionPool(sqlConfig.getIp(), sqlConfig.getPort(), sqlConfig.getDatabase(),
                    sqlConfig.getUsername(), sqlConfig.getPassword(),
                    ValueUtil.isEmpty(sqlConfig.getConnectUrl()) ? MysqlConnectionPool.defaultUrl : sqlConfig.getConnectUrl()
            );
        } else if (sqlConfig.getBackend() == SqlConfig.SqlBackend.H2) {
            pool = new H2ConnectionPool(core.getPlugin().getDataFolder(), sqlConfig.getUsername(), sqlConfig.getPassword(),
                    ValueUtil.isEmpty(sqlConfig.getConnectUrl()) ? H2ConnectionPool.defaultUrl : sqlConfig.getConnectUrl()
            );
        } else {
            throw new UnsupportedOperationException("Database type Unknown.");
        }
        String tablePrefix = sqlConfig.getTablePrefix() + '_';

        final String inGameProfileTableName = tablePrefix + "in_game_profile_v2";
        final String userDataTableName = tablePrefix + "user_data_v2";
        final String skinRestorerCacheTableName = tablePrefix + "skin_restored_cache_v2";
        inGameProfileTable = new InGameProfileTable(this, inGameProfileTableName);
        userDataTable = new UserDataTable(this, userDataTableName);
        skinRestoredCacheTable = new SkinRestoredCacheTable(this, skinRestorerCacheTableName);
        inGameProfileTable.init();
        userDataTable.init();
        skinRestoredCacheTable.init();
    }

    public void close() {
        if (pool != null) pool.close();
    }
}
