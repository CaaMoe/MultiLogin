package moe.caa.multilogin.dataupgrade.newc;

import moe.caa.multilogin.dataupgrade.ValueUtil;
import moe.caa.multilogin.dataupgrade.oldc.OldUserData;
import moe.caa.multilogin.dataupgrade.sql.Backend;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

public class NewSQLHandler {
    private final URLClassLoader urlClassLoader;
    private final Driver driver;
    private final Connection connection;
    private final String inGameProfileTableName;
    private final String userDataTableName;

    public NewSQLHandler(File folder, NewConfig config) throws Throwable {
        String tablePrefix = config.getS_tablePrefix() + "_";
        inGameProfileTableName = tablePrefix + "in_game_profile_v2";
        userDataTableName = tablePrefix + "user_data_v2";

        Properties properties = new Properties();
        properties.put("user", config.getS_username());
        properties.put("password", config.getS_password());

        if (config.getS_backend() == Backend.H2) {
            urlClassLoader = new URLClassLoader(new URL[]{new URL("https://maven.aliyun.com/repository/public/com/h2database/h2/2.1.212/h2-2.1.212.jar")}, ClassLoader.getSystemClassLoader().getParent());
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

        connection.prepareStatement(MessageFormat.format("CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} VARCHAR(64) DEFAULT NULL, " +
                        "CONSTRAINT IGPT_PR PRIMARY KEY ( {1} ), " +
                        "CONSTRAINT IGPT_UN UNIQUE ( {2} ))"
                , inGameProfileTableName, "in_game_uuid", "current_username")).executeUpdate();

        connection.prepareStatement(MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ( " +
                        "{1} BINARY(16) NOT NULL, " +
                        "{2} BINARY(1) NOT NULL, " +
                        "{3} BINARY(16) DEFAULT NULL, " +
                        "{4} BOOL DEFAULT FALSE, " +
                        "PRIMARY KEY ( {1}, {2} ))"
                , userDataTableName, "online_uuid", "yggdrasil_id", "in_game_profile_uuid", "whitelist")).executeUpdate();
    }

    public void insertNewUserData(int yggdrasilId, OldUserData userData) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO " + inGameProfileTableName + "(in_game_uuid, current_username) VALUES(?, ?)");
        preparedStatement.setBytes(1, ValueUtil.uuidToBytes(userData.getRedirectUUID()));
        preparedStatement.setString(2, userData.getCurrentName());
        preparedStatement.executeUpdate();

        PreparedStatement preparedStatement2 =
                connection.prepareStatement("INSERT INTO " + userDataTableName + "(online_uuid, yggdrasil_id, in_game_profile_uuid, whitelist) VALUES(?, ?, ?, ?)");
        preparedStatement2.setBytes(1, ValueUtil.uuidToBytes(userData.getOnlineUUID()));
        preparedStatement2.setInt(2, yggdrasilId);
        preparedStatement2.setBytes(3, ValueUtil.uuidToBytes(userData.getRedirectUUID()));
        preparedStatement2.setBoolean(4, userData.isWhitelist());
        preparedStatement2.executeUpdate();
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            urlClassLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
