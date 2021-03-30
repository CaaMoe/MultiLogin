package moe.caa.multilogin.core.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class YamlConfig {
    private static final Yaml YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(dumperOptions);
    }

    private final Map<?, ?> contents;

    private YamlConfig(Map<?, ?> contents) {
        this.contents = contents == null ? new LinkedHashMap<>() : contents;
    }

    public static YamlConfig fromReader(Reader reader) {
        return new YamlConfig(YAML.loadAs(reader, LinkedHashMap.class));
    }

    public void save(Writer writer) {
        YAML.dump(contents, writer);
    }

    public Optional<?> get(String path) {
        return Optional.ofNullable(contents.get(path));
    }

    public Optional<String> getString(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof String) {
            return Optional.of((String) value.get());
        }
        return Optional.empty();
    }

    public String getStringOrElse(String path, String other) {
        return getString(path).orElse(other);
    }

    public Optional<Integer> getInteger(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof Integer) {
            return Optional.of((Integer) value.get());
        }
        return Optional.empty();
    }

    public int getIntegerOrElse(String path, int other) {
        return getInteger(path).orElse(other);
    }

    public Optional<Long> getLong(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof Long) {
            return Optional.of((Long) value.get());
        }
        return Optional.empty();
    }

    public Long getLongOrElse(String path, long other) {
        return getLong(path).orElse(other);
    }

    public Optional<Double> getDouble(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof Double) {
            return Optional.of((Double) value.get());
        }
        return Optional.empty();
    }

    public double getDoubleOrElse(String path, double other) {
        return getDouble(path).orElse(other);
    }

    public Optional<Boolean> getBoolean(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof Boolean) {
            return Optional.of((Boolean) value.get());
        }
        return Optional.empty();
    }

    public boolean getBooleanOrElse(String path, boolean other) {
        return getBoolean(path).orElse(other);
    }

    public Optional<YamlConfig> getSection(String path) {
        Optional<?> value = get(path);
        if (value.isPresent() && value.get() instanceof Map) {
            return Optional.of(new YamlConfig((Map<?, ?>) value.get()));
        }
        return Optional.empty();
    }

    public void set(String path, Object value) {
        // contents.put(path, value);
        // TODO: 2021/3/30  
    }

    public Set<String> getKeys() {
        return contents.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }
}
