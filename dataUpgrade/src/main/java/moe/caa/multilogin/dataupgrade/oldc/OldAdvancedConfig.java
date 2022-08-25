package moe.caa.multilogin.dataupgrade.oldc;

import lombok.Getter;
import lombok.ToString;

import java.util.Properties;

/**
 * 读 advanced_setting.properties 文件
 */
@ToString
@Getter
public class OldAdvancedConfig {
    private final String database_user_data_table_name;
    private final String database_cache_whitelist_table_name;
    private final String database_skin_restorer_table_name;

    public OldAdvancedConfig(Properties properties){
        database_user_data_table_name = properties.getProperty("database_user_data_table_name", "{prefix}{_}user_data");
        database_cache_whitelist_table_name = properties.getProperty("database_cache_whitelist_table_name", "{prefix}{_}cache_whitelist");
        database_skin_restorer_table_name = properties.getProperty("database_skin_restorer_table_name", "{prefix}{_}skin_restorer");
    }
}
