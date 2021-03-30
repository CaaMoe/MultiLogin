package moe.caa.multilogin.core.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConfig {
    private final Map<?, ?> contents;
    private final Map<?, ?> defaultContents;
    private static final Yaml YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(dumperOptions);
    }

    private YamlConfig(Map<?, ?> contents, Map<?, ?> defaultContents){
        this.contents = contents == null ? new LinkedHashMap<>() : contents;
        this.defaultContents = defaultContents == null ? new LinkedHashMap<>() : defaultContents;
    }

    public void save(Writer writer){
        YAML.dump(contents, writer);
    }

    public static YamlConfig fromReader(Reader reader, Reader defaultReader){
        Map<?, ?> contents = YAML.loadAs(reader, LinkedHashMap.class);
        Map<?, ?> defaultContents = YAML.loadAs(defaultReader, LinkedHashMap.class);
        return new YamlConfig(contents, defaultContents);
    }
    
    public String getString(String path){
        return (String)contents.get(path);
    }
}
