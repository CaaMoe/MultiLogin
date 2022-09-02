package moe.caa.multilogin.dataupgrade;

import moe.caa.multilogin.dataupgrade.oldc.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据升级程序未完成
 */
public class Main {
    public static List<OldUserData> oldUserDataList;
    private static OldConfig oldConfig;

    public static void main(String[] args) throws InterruptedException {
        long timeMillis = System.currentTimeMillis();
        readOldData();
        if(oldUserDataList == null || oldConfig == null) {
            return;
        }
        System.out.println("Checking import...");
        Map<String, Set<OldUserData>> checkResultMap = checkImport();
        if(checkResultMap.size() != 0){
            System.out.println();
            System.err.println("================================================================");
            for (Map.Entry<String, Set<OldUserData>> entry : checkResultMap.entrySet()) {
                System.err.println(" Yggdrasil Service with path " + entry.getKey() + " is not found, this will affect the " + entry.getValue().size() + " bar data!");
            }
            System.err.println(" The affected data will not be upgraded!!!");
            System.err.println(" Will continue in 15 seconds. Abort immediately if necessary.");
            System.err.println("================================================================");
            Thread.sleep(15000);
        }

        System.out.println("================================================================");
        System.out.println(" Allocated Yggdrasil ID:");
        for (int i = 0; i < oldConfig.getServices().size(); i++) {
            System.out.printf("      ID: %d is allocated to the Yggdrasil Service where path is %s and name is %s.%n", i
            , oldConfig.getServices().get(i).getPath(), oldConfig.getServices().get(i).getName());
        }
        System.out.println();
        System.out.println(" Unhappy with the allocation? You can immediately close the program, change the order of the service children in the configuration file, and run the program again.");
        System.out.println(" Writing will begin in 15 seconds.");
        System.out.println("================================================================");
        Thread.sleep(15000);
        System.out.println("Converting data...");

        try {
            convertAndWrite();
        } catch (Exception e) {
            System.err.println("An exception occurs when processing upgrade data.");
            e.printStackTrace();
            return;
        }
        System.out.printf("\n\nDone. Total time: %.2f seconds.", ((System.currentTimeMillis() - timeMillis) + 1.0) / 1000);
    }

    public static void convertAndWrite() throws ConfigurateException {
        System.out.println("Converting configuration...");

    }

    /**
     * 检查数据完整
     */
    public static Map<String, Set<OldUserData>> checkImport() {
        Set<String> set = oldConfig.getServices().stream().map(OldYggdrasilConfig::getPath).collect(Collectors.toSet());

        Map<String, Set<OldUserData>> lossPath = new HashMap<>();
        for (OldUserData data : oldUserDataList) {
            if (set.contains(data.getYggdrasilService())) continue;
            Set<OldUserData> lp = lossPath.getOrDefault(data.getYggdrasilService(), new HashSet<>());
            lp.add(data);
            lossPath.put(data.getYggdrasilService(), lp);
        }
        return lossPath;
    }

    // 读老数据
    public static void readOldData(){
        File inputFile = new File("input");
        File configFile = new File(inputFile, "config.yml");
        File advancedConfigFile = new File(inputFile, "advanced_setting.properties");

        if (!configFile.exists()) {
            System.err.println("The config.yml file could not be found.");
            System.err.println("You need to create the input folder in the program's sibling directory, and then put the old files into it, Try again.");
            return;
        }

        // 解析 config.yml 文件
        CommentedConfigurationNode configurationNodeConfigYml;
        try {
            configurationNodeConfigYml = YamlConfigurationLoader.builder().file(configFile).build().load();
        } catch (ConfigurateException e) {
            System.err.println("An exception occurred while reading the config.yml file, maybe it's damaged.");
            e.printStackTrace();
            return;
        }

        // 老文件是必须有这个节点的，没有则代表是坏的。
        if (!configurationNodeConfigYml.hasChild("services")) {
            System.err.println("No services configuration was found.");
            System.err.println("Has it been successfully upgraded before?");
            return;
        }

        // 读取他的全部配置
        final OldConfig oldConfig;
        try {
            oldConfig = new OldConfig(configurationNodeConfigYml);
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
            System.out.println("Loading the old " + oldConfig.getS_backend().name().toLowerCase() + " database driver...");
            oldSQLHandler = new OldSQLHandler(inputFile, oldConfig, oldAdvancedConfig);
        } catch (Throwable e){
            System.err.println("Cannot process old database, please check.");
            e.printStackTrace();
            return;
        }

        List<OldUserData> oldUserData;
        try {
            System.out.println("Importing data...");
            oldUserData = oldSQLHandler.readAll();
        } catch (SQLException e) {
            System.err.println("Cannot read old data.");
            e.printStackTrace();
            return;
        }

        System.out.println(oldUserData.size() + " user data have been imported.");
        System.out.println(oldConfig.getServices().size() + " yggdrasil service have been imported.");

        Main.oldConfig = oldConfig;
        Main.oldUserDataList = oldUserData;
    }
}
