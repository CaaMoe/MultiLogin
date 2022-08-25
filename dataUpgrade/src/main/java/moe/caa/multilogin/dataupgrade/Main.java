package moe.caa.multilogin.dataupgrade;

import moe.caa.multilogin.dataupgrade.oldc.OldAdvancedConfig;
import moe.caa.multilogin.dataupgrade.oldc.OldConfig;
import moe.caa.multilogin.dataupgrade.oldc.OldSQLHandler;
import moe.caa.multilogin.dataupgrade.oldc.OldUserData;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * 数据升级程序未完成
 */
public class Main {

    public static void main(String[] args) {

        // 取 input 文件夹
        File inputFile = new File("input");
        File configFile = new File(inputFile, "config.yml");
        File advancedConfigFile = new File(inputFile, "advanced_setting.properties");
        if (!configFile.exists()) {
            System.err.println("The config.yml file could not be found.");
            System.err.println("You need to create the input folder in the program's sibling directory, and then put the old files into it, Try again.");
            return;
        }

        // 解析 config.yml 文件
        CommentedConfigurationNode load;
        try {
            load = YamlConfigurationLoader.builder().file(configFile).build().load();
        } catch (ConfigurateException e) {
            System.err.println("An exception occurred while reading the config.yml file, maybe it's damaged.");
            e.printStackTrace();
            return;
        }

        // 老文件是必须有这个节点的，没有则代表是坏的。
        if (!load.hasChild("services")) {
            System.err.println("No services configuration was found.");
            System.err.println("Has it been successfully upgraded before?");
        }

        // 读取他的全部配置
        final OldConfig oldConfig;
        try {
            oldConfig = new OldConfig(load);
        } catch (Throwable e) {
            System.err.println("An exception occurred while parsing the config.yml file, maybe it's damaged.");
            e.printStackTrace();
            return;
        }

        // 读取高级设置文件
        final OldAdvancedConfig oldAdvancedConfig;
        if(advancedConfigFile.exists()){
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(advancedConfigFile));
                oldAdvancedConfig = new OldAdvancedConfig(properties);
            } catch (Exception e) {
                System.err.println("An exception occurred while parsing the advanced_setting.properties file, maybe it's damaged.");
                e.printStackTrace();
                return;
            }
        } else {
            oldAdvancedConfig = new OldAdvancedConfig(new Properties());
        }

        // 读数据
        OldSQLHandler oldSQLHandler;
        try {
            System.out.println("Loading the old database driver...");
            oldSQLHandler = new OldSQLHandler(inputFile, oldConfig, oldAdvancedConfig);
        } catch (Throwable e){
            System.err.println("Cannot process old database, please check.");
            e.printStackTrace();
            return;
        }

        List<OldUserData> oldUserData;
        try {
            oldUserData = oldSQLHandler.readAll();
        } catch (SQLException e) {
            System.err.println("Cannot read old data.");
            e.printStackTrace();
            return;
        }

        System.out.println(oldUserData.size() + " data is imported");


        for (OldConfig.OldYggdrasilConfig service : oldConfig.getServices()) {
            System.out.printf("Resolved a yggdrasil service with path %s and name %s.%n", service.getPath(), service.getName());
        }
    }
}
