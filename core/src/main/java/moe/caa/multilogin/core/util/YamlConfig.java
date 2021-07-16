package moe.caa.multilogin.core.util;

import moe.caa.multilogin.core.NoSuchEnumException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Yaml 配置文件读写类
 */
@SuppressWarnings("all")
public final class YamlConfig {
    private static final Yaml YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(dumperOptions);
    }

    private final Map<String, Object> CONTENT;

    private YamlConfig(Map<String, Object> content) {
        this.CONTENT = ValueUtil.getOrDef(content, new LinkedHashMap<>());
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
     * 获得当前节点下所有的真子节点
     * @return 真子节点
     */
    public Set<String> getKeys(){
        return CONTENT.keySet();
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
        Object val = CONTENT.get(path);
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

    public void set(String path, Object newValue) {
        CONTENT.put(path, newValue);
    }
}
