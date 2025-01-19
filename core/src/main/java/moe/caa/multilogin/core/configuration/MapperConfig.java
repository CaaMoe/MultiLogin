package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ChatSessionBlocker 数据包映射配置
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class MapperConfig {
    private final LinkedHashMap<String,Integer> packetMapping;
    public static MapperConfig read(CommentedConfigurationNode rootNode) throws SerializationException, ConfException {
        LinkedHashMap<String,Integer> mapper = new LinkedHashMap<>() {{
            put("MINECRAFT_1_19_3", 0x20);
            put("MINECRAFT_1_19_4", 0x06);
            put("MINECRAFT_1_20_5", 0x07);
            put("MINECRAFT_1_21_2", 0x08);
        }};
        ConfigurationNode mapperNode = rootNode.node("mapper");
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : mapperNode.childrenMap().entrySet()) {
            String key = entry.getKey().toString();
            String hexValue = entry.getValue().getString();
            int intValue = Integer.decode(hexValue);
            mapper.put(key, intValue);
        }
        return new MapperConfig(mapper);
    }
}
