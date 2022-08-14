package moe.caa.multilogin.dataupgrade;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

/**
 * 数据升级程序未完成
 */
public class Main {

    public static void main(String[] args) {
        File file = new File("config.yml");
        if (!file.exists()) {
            System.err.println("The config.yml file could not be found.");
            System.err.println("Please move this program to the same directory as the config.yml file and try running it again.");
            return;
        }

        CommentedConfigurationNode load;
        try {
            load = YamlConfigurationLoader.builder().file(file).build().load();
        } catch (ConfigurateException e) {
            System.err.println("An exception occurred while reading the config.yml file.");
            e.printStackTrace();
            return;
        }

        if (!load.hasChild("services")) {
            System.err.println("No services configuration was found.");
            System.err.println("Has it been successfully upgraded before?");
        }



//
//
//        try {
//            Files.move(file.toPath(), new File("config.yml.old-" + System.currentTimeMillis()).toPath());
//        } catch (IOException e) {
//            System.err.println("An exception occurred while operating the file.");
//            e.printStackTrace();
//            return;
//        }
    }
}
