package moe.caa.multilogin.core.util;

import lombok.var;
import moe.caa.multilogin.core.exception.NoSuchEnumException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * YAML 配置文件读取类
 */
public class YamlConfig {
    private static final Yaml YAML;

    static {
        var dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(dumperOptions);
    }

    private final Map<String, Object> content;

    private YamlConfig(Map<String, Object> content) {
        this.content = ValueUtil.getOrDef(content, new LinkedHashMap<>());
    }

    /**
     * 从 InputStream 对象构建这个 YamlConfig
     *
     * @param input 输入流
     * @return YamlConfig
     */
    public static YamlConfig fromInputStream(InputStream input) {
        return new YamlConfig(YAML.loadAs(new InputStreamReader(input, StandardCharsets.UTF_8), LinkedHashMap.class));
    }

    /**
     * 返回空的配置文件对象
     * @return 空的配置文件对象
     */
    public static YamlConfig empty() {
        return new YamlConfig(null);
    }

    /**
     * 获得当前节点下所有的真子节点
     *
     * @return 真子节点
     */
    public Set<String> getKeys() {
        return content.keySet();
    }

    /**
     * 获取某个节点的值
     *
     * @param path 路径
     * @param type 期望得到的类型的 class 对象
     * @param <R>  期望得到的类型
     * @return 值
     */
    public <R> R get(String path, Class<R> type) {
        var val = content.get(path);
        if (type.isEnum()) {
            if (val instanceof String)
                try {
                    return (R) ReflectUtil.getEnumIns((Class<? extends Enum<?>>) type, (String) val);
                } catch (NoSuchEnumException ignore) {
                }
            return null;
        }

        if (type == getClass()) {
            if (!(val instanceof Map)) return null;
            return (R) new YamlConfig((Map) val);
        }
        return val != null ? ReflectUtil.isCaseClass(val.getClass(), type) ? (R) val : null : null;
    }

    /**
     * 获取某个节点的值
     *
     * @param path 路径
     * @param type 期望得到的类型的 class 对象
     * @param def  获取为 null 时返回此值
     * @param <R>  期望得到的类型
     * @return 值
     */
    public <R> R get(String path, Class<R> type, R def) {
        return ValueUtil.getOrDef(get(path, type), def);
    }
}

