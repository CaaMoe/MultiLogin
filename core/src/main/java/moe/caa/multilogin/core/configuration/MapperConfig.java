package moe.caa.multilogin.core.configuration;

import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.MapperConfigAPI;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.*;

/**
 * ChatSessionBlocker 数据包映射配置
 */
@Getter
@ToString
public class MapperConfig implements MapperConfigAPI {
    private final TreeMap<Integer,Integer> packetMapping = new TreeMap<>() {
        @Override
        public Integer put(Integer key, Integer value) {
            if(key<761) return value;
            if(this.containsValue(value)) {
                Integer existingKey = findKeyByValue(value);
                if (existingKey != null && existingKey > key) {
                    super.remove(existingKey);
                    super.put(key, value);
                }
                return value;
            }
            return super.put(key,value);
        }
        private Integer findKeyByValue(Integer value) {
            for (Map.Entry<Integer, Integer> entry : this.entrySet()) {
                if (entry.getValue().equals(value)) {
                    return entry.getKey();
                }
            }
            return null;
        }
        {
            put(761,0x20);
            put(762,0x06);
            put(765,0x07);
            put(768,0x08);
            put(771,0x09);
        }
    };

    private final File dataFolder;
    MapperConfig(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public void save() {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder().file(new File(dataFolder, "mapper.yml")).indent(2).build();
            CommentedConfigurationNode rootNode = loader.load();
            CommentedConfigurationNode mapperNode = rootNode.node("mapper");
            for (Map.Entry<Integer, Integer> entry : packetMapping.entrySet()) {
                mapperNode.node(entry.getKey().toString()).set(String.format("0x%02X", entry.getValue()));
            }
            loader.save(rootNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().file(new File(dataFolder, "mapper.yml")).build();
        try {
            ConfigurationNode mapperNode = loader.load().node("mapper");
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : mapperNode.childrenMap().entrySet()) {
                String key = entry.getKey().toString();
                String hexValue = entry.getValue().getString();
                if (hexValue != null) {
                    int intValue = Integer.decode(hexValue);
                    packetMapping.put(Integer.parseInt(key), intValue);
                }
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
