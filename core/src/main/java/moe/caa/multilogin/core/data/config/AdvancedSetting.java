package moe.caa.multilogin.core.data.config;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.core.util.ValueUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

/**
 * 高级配置阅读程序
 */
@Getter
@ToString
public class AdvancedSetting {
    private transient final Properties properties = new Properties();
    private String database_user_data_table_name = "{prefix}{_}user_data";
    private String database_user_data_table_field_online_uuid = "online_uuid";
    private String database_user_data_table_field_current_name = "current_name";
    private String database_user_data_table_field_redirect_uuid = "redirect_uuid";
    private String database_user_data_table_field_yggdrasil_service = "yggdrasil_service";
    private String database_user_data_table_field_whitelist = "whitelist";
    private String database_cache_whitelist_table_name = "{prefix}{_}cache_whitelist";
    private String database_cache_whitelist_table_field_sign = "sign";
    private String database_backend_url_h2 = "jdbc:h2:{file_path};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
    private String database_backend_url_mysql = "jdbc:mysql://{ip}:{port}/{database}?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
    private boolean verify_yggdrasil_ignore_path = false;
    private boolean verify_username_allow_duplicate_name = false;
    private boolean load_failed_exit_server = true;
    private boolean logger_debug = false;

    public AdvancedSetting(InputStream stream) throws IOException, IllegalAccessException {
        properties.load(stream);
        read();
    }

    private void read() throws IllegalAccessException {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers())) continue;
            String path = field.getName();
            if (field.getType() == String.class) {
                field.set(this, properties.getProperty(path, (String) field.get(this)));
            } else if (field.getType() == boolean.class) {
                String value = properties.getProperty(path);
                if (ValueUtil.isBoolValue(value)) {
                    field.set(this, Boolean.parseBoolean(value));
                }
            }
        }
    }
}
