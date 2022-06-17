package moe.caa.multilogin.core.database;

import lombok.Getter;
import moe.caa.multilogin.core.configuration.backend.BackendConfig;
import moe.caa.multilogin.core.configuration.backend.BackendType;
import moe.caa.multilogin.core.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.database.table.InGameProfileTable;
import moe.caa.multilogin.core.database.table.UserDataTable;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;


public class SQLManager {
    @Getter
    private final MultiCore core;
    @Getter
    private ISQLConnectionPool pool;
    @Getter
    private InGameProfileTable inGameProfileTable;
    @Getter
    private UserDataTable userDataTable;


    public SQLManager(MultiCore core) {
        this.core = core;
    }

    public void init() throws SQLException, ClassNotFoundException {
        BackendConfig backendConfig = core.getPluginConfig().getBackendConfig();
        if (backendConfig.getBackend() == BackendType.MYSQL) {
            pool = new MysqlConnectionPool(backendConfig.getIp(), backendConfig.getPort(), backendConfig.getDatabase(),
                    backendConfig.getUsername(), backendConfig.getPassword()
            );
        } else if (backendConfig.getBackend() == BackendType.H2) {
            pool = new H2ConnectionPool(core.getPlugin().getDataFolder(), backendConfig.getUsername(), backendConfig.getPassword());
        } else {
            throw new UnsupportedOperationException("Database type Unknown.");
        }
        String tablePrefix = backendConfig.getTablePrefix() + '_';

        final String inGameProfileTableName = tablePrefix + "in_game_profile_v2";
        final String userDataTableName = tablePrefix + "user_data_v2";
        inGameProfileTable = new InGameProfileTable(this, inGameProfileTableName);
        userDataTable = new UserDataTable(this, userDataTableName);
        inGameProfileTable.init();
        userDataTable.init();
    }
}
