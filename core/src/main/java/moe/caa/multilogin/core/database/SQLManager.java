package moe.caa.multilogin.core.database;

import lombok.Getter;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.configuration.SqlConfig;
import moe.caa.multilogin.core.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.database.table.InGameProfileTableV3;
import moe.caa.multilogin.core.database.table.SkinRestoredCacheTableV2;
import moe.caa.multilogin.core.database.table.UserDataTableV3;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.Connection;
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
    private InGameProfileTableV3 inGameProfileTable;
    @Getter
    private UserDataTableV3 userDataTable;
    @Getter
    private SkinRestoredCacheTableV2 skinRestoredCacheTable;


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

        final String inGameProfileTableNameV2 = tablePrefix + "in_game_profile_v2";
        final String inGameProfileTableNameV3 = tablePrefix + "in_game_profile_v3";
        final String userDataTableNameV2 = tablePrefix + "user_data_v2";
        final String userDataTableNameV3 = tablePrefix + "user_data_v3";
        final String skinRestorerCacheTableNameV2 = tablePrefix + "skin_restored_cache_v2";
        userDataTable = new UserDataTableV3(this, userDataTableNameV3, userDataTableNameV2);
        skinRestoredCacheTable = new SkinRestoredCacheTableV2(this, skinRestorerCacheTableNameV2);
        inGameProfileTable = new InGameProfileTableV3(this, inGameProfileTableNameV3, inGameProfileTableNameV2);

        try (Connection connection = getPool().getConnection()){
            connection.setAutoCommit(false);
            userDataTable.init(connection);
            inGameProfileTable.init(connection);
            skinRestoredCacheTable.init(connection);
            connection.commit();
        }
    }

    public void close() {
        if (pool != null) pool.close();
    }
}
