package moe.caa.multilogin.dataupgrade.oldc;

import moe.caa.multilogin.dataupgrade.ValueUtil;
import moe.caa.multilogin.dataupgrade.sql.Backend;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 老的数据库处理程序
 */
public class OldSQLHandler {
    private final URLClassLoader urlClassLoader;
    private final Driver driver;
    private final Connection connection;
    private final String tableName;

    public OldSQLHandler(File folder, OldConfig config, OldAdvancedConfig advancedConfig) throws Throwable {
        tableName = advancedConfig.getDatabase_user_data_table_name()
                .replace("{prefix}", config.getS_prefix())
                .replace("{_}", config.getS_prefix().trim().length() != 0 ? "_" : "");

        Properties properties = new Properties();
        properties.put("user", config.getS_username());
        properties.put("password", config.getS_password());

        if (config.getS_backend() == Backend.H2) {
            urlClassLoader = new URLClassLoader(new URL[]{new URL("https://maven.aliyun.com/repository/public/com/h2database/h2/1.4.200/h2-1.4.200.jar")}, ClassLoader.getSystemClassLoader().getParent());
            Class<?> aClass = Class.forName("org.h2.Driver", true, urlClassLoader);
            Field driverField = aClass.getDeclaredField("INSTANCE");
            driverField.setAccessible(true);
            driver = ((Driver) driverField.get(null));
            connection = driver.connect("jdbc:h2:" + new File(folder, "multilogin").getAbsolutePath(), properties);
        } else {
            urlClassLoader = new URLClassLoader(new URL[]{
                    new URL("https://maven.aliyun.com/repository/public/mysql/mysql-connector-java/8.0.25/mysql-connector-java-8.0.25.jar"),
                    new URL("https://maven.aliyun.com/repository/public/io/leangen/geantyref/geantyref/1.3.13/geantyref-1.3.13.jar")
            }, ClassLoader.getSystemClassLoader().getParent());
            Class<?> aClass = Class.forName("com.mysql.cj.jdbc.Driver", true, urlClassLoader);
            driver = ((Driver) aClass.getConstructor().newInstance());

            connection = driver.connect("jdbc:mysql://" + config.getS_ip() + ":" + config.getS_port() + "/" + config.getS_database(), properties);
        }

    }

    public List<OldUserData> readAll() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT online_uuid, current_name, redirect_uuid, yggdrasil_service, whitelist FROM " + tableName);
        ResultSet resultSet = statement.executeQuery();
        List<OldUserData> oldUserData = new ArrayList<>();
        while (resultSet.next()) {
            oldUserData.add(
                    new OldUserData(
                            ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                            resultSet.getString(2),
                            ValueUtil.bytesToUuid(resultSet.getBytes(3)),
                            resultSet.getString(4),
                            resultSet.getBoolean(5)
                    )
            );
        }
        return oldUserData;
    }
}
